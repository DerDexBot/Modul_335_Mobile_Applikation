#include <furi.h>
#include <furi_hal_serial.h>
#include <furi_hal_serial_control.h>
#include <gui/elements.h>
#include <gui/gui.h>
#include <input/input.h>
#include <nfc/nfc.h>
#include <nfc/nfc_poller.h>
#include <nfc/protocols/iso14443_4a/iso14443_4a_poller.h>
#include <toolbox/bit_buffer.h>

#include <stdio.h>
#include <string.h>

#define TAG "AuthHceReader"

#define AUTH_RX_BUFFER_SIZE      256U
#define AUTH_DISPLAY_TEXT_SIZE   192U
#define AUTH_DISPLAY_DETAIL_SIZE 64U
#define AUTH_FIELD_SESSION_SIZE  24U
#define AUTH_FIELD_USERNAME_SIZE 32U
#define AUTH_FIELD_ACTION_SIZE   16U
#define AUTH_FIELD_CHALLENGE_SIZE 96U
#define AUTH_UART_LINE_SIZE      48U
#define AUTH_UART_BAUD_RATE      115200U

static const uint8_t flipper_auth_select_apdu[] = {
    0x00,
    0xA4,
    0x04,
    0x00,
    0x0C,
    0xF0,
    0x46,
    0x4C,
    0x49,
    0x50,
    0x50,
    0x45,
    0x52,
    0x41,
    0x55,
    0x54,
    0x48,
    0x00,
};

typedef enum {
    FlipperAuthViewStateScanning,
    FlipperAuthViewStatePayloadRead,
    FlipperAuthViewStateNoSession,
    FlipperAuthViewStateFormatError,
    FlipperAuthViewStateApduError,
    FlipperAuthViewStateNfcError,
} FlipperAuthViewState;

typedef enum {
    FlipperAuthAppEventTypeInput,
    FlipperAuthAppEventTypeNfcDone,
} FlipperAuthAppEventType;

typedef struct {
    FlipperAuthAppEventType type;
    InputEvent input;
} FlipperAuthAppEvent;

typedef struct {
    Gui* gui;
    ViewPort* view_port;
    FuriMessageQueue* event_queue;
    FuriMutex* state_mutex;

    Nfc* nfc;
    NfcPoller* poller;
    FuriHalSerialHandle* serial_handle;
    BitBuffer* tx_buffer;
    BitBuffer* rx_buffer;

    FlipperAuthViewState view_state;
    char payload[AUTH_DISPLAY_TEXT_SIZE];
    char detail[AUTH_DISPLAY_DETAIL_SIZE];
    char session_id[AUTH_FIELD_SESSION_SIZE];
    char username[AUTH_FIELD_USERNAME_SIZE];
    char action[AUTH_FIELD_ACTION_SIZE];
    bool uart_send_pending;
    bool uart_sent;
} FlipperAuthApp;

static const char* flipper_auth_state_title(FlipperAuthViewState state) {
    switch(state) {
    case FlipperAuthViewStateScanning:
        return "Scanning...";
    case FlipperAuthViewStatePayloadRead:
        return "AID OK";
    case FlipperAuthViewStateNoSession:
        return "Keine Session";
    case FlipperAuthViewStateFormatError:
        return "Format unerwartet";
    case FlipperAuthViewStateApduError:
        return "APDU Fehler";
    case FlipperAuthViewStateNfcError:
        return "NFC Fehler";
    default:
        return "Status";
    }
}

static void flipper_auth_draw_callback(Canvas* canvas, void* context) {
    furi_assert(context);
    FlipperAuthApp* app = context;

    furi_mutex_acquire(app->state_mutex, FuriWaitForever);

    canvas_clear(canvas);
    canvas_set_font(canvas, FontPrimary);
    canvas_draw_str(canvas, 2, 10, "Auth HCE Reader");

    canvas_set_font(canvas, FontSecondary);
    if(app->view_state == FlipperAuthViewStatePayloadRead) {
        char line[64];
        canvas_draw_str(canvas, 2, 20, "AID OK");
        snprintf(line, sizeof(line), "sessionId: %s", app->session_id);
        canvas_draw_str(canvas, 2, 31, line);
        snprintf(line, sizeof(line), "username: %s", app->username);
        canvas_draw_str(canvas, 2, 42, line);
        snprintf(line, sizeof(line), "action: %s", app->action);
        canvas_draw_str(canvas, 2, 53, line);
    } else {
        canvas_draw_str(canvas, 2, 23, flipper_auth_state_title(app->view_state));
        canvas_draw_str(canvas, 2, 34, app->detail);

        if(app->payload[0] != '\0') {
            elements_text_box(canvas, 0, 37, 128, 20, AlignLeft, AlignTop, app->payload, true);
        } else if(app->view_state == FlipperAuthViewStateScanning) {
            elements_text_box(
                canvas,
                0,
                37,
                128,
                20,
                AlignLeft,
                AlignTop,
                "Android entsperren und an NFC halten.",
                true);
        }

        if(app->view_state == FlipperAuthViewStateScanning) {
            elements_button_left(canvas, "Back");
        } else {
            elements_button_left(canvas, "Back");
            elements_button_center(canvas, "Retry");
        }
    }

    furi_mutex_release(app->state_mutex);
}

static void flipper_auth_input_callback(InputEvent* input_event, void* context) {
    furi_assert(context);
    FuriMessageQueue* event_queue = context;

    FlipperAuthAppEvent app_event = {
        .type = FlipperAuthAppEventTypeInput,
        .input = *input_event,
    };
    furi_message_queue_put(event_queue, &app_event, FuriWaitForever);
}

static bool flipper_auth_payload_has_required_fields(const char* payload) {
    return strncmp(payload, "AUTH|", 5) == 0 && strstr(payload, "sessionId=") &&
           strstr(payload, "|username=") && strstr(payload, "|action=") &&
           strstr(payload, "|challenge=");
}

static bool flipper_auth_is_digits(const char* value) {
    if(value[0] == '\0') {
        return false;
    }

    for(size_t i = 0; value[i] != '\0'; i++) {
        if((value[i] < '0') || (value[i] > '9')) {
            return false;
        }
    }

    return true;
}

static bool
    flipper_auth_extract_field(const char* payload, const char* key, char* output, size_t output_size) {
    furi_assert(payload);
    furi_assert(key);
    furi_assert(output);
    furi_assert(output_size > 0);

    output[0] = '\0';

    const char* value_start = strstr(payload, key);
    if(!value_start) {
        return false;
    }

    value_start += strlen(key);

    size_t value_size = 0;
    while(value_start[value_size] && value_start[value_size] != '|') {
        value_size++;
    }

    if((value_size == 0) || (value_size >= output_size)) {
        return false;
    }

    memcpy(output, value_start, value_size);
    output[value_size] = '\0';

    return true;
}

static void flipper_auth_clear_payload_fields(FlipperAuthApp* app) {
    app->session_id[0] = '\0';
    app->username[0] = '\0';
    app->action[0] = '\0';
    app->uart_send_pending = false;
    app->uart_sent = false;
}

static void flipper_auth_set_status(
    FlipperAuthApp* app,
    FlipperAuthViewState state,
    const char* detail,
    const char* payload) {
    furi_mutex_acquire(app->state_mutex, FuriWaitForever);

    app->view_state = state;
    snprintf(app->detail, sizeof(app->detail), "%s", detail ? detail : "");
    snprintf(app->payload, sizeof(app->payload), "%s", payload ? payload : "");
    if(state != FlipperAuthViewStatePayloadRead) {
        flipper_auth_clear_payload_fields(app);
    }

    furi_mutex_release(app->state_mutex);
}

static void flipper_auth_set_payload_read(
    FlipperAuthApp* app,
    const char* payload,
    const char* session_id,
    const char* username,
    const char* action) {
    furi_mutex_acquire(app->state_mutex, FuriWaitForever);

    app->view_state = FlipperAuthViewStatePayloadRead;
    snprintf(app->detail, sizeof(app->detail), "%s", "AID OK");
    snprintf(app->payload, sizeof(app->payload), "%s", payload);
    snprintf(app->session_id, sizeof(app->session_id), "%s", session_id);
    snprintf(app->username, sizeof(app->username), "%s", username);
    snprintf(app->action, sizeof(app->action), "%s", action);
    app->uart_send_pending = true;
    app->uart_sent = false;

    furi_mutex_release(app->state_mutex);
}

static void flipper_auth_parse_response(FlipperAuthApp* app, Iso14443_4aError iso_error) {
    if(iso_error != Iso14443_4aErrorNone) {
        flipper_auth_set_status(app, FlipperAuthViewStateNfcError, "ISO14443-4A fehlgeschlagen", "");
        return;
    }

    const size_t rx_size = bit_buffer_get_size_bytes(app->rx_buffer);
    const uint8_t* rx_data = bit_buffer_get_data(app->rx_buffer);

    if(rx_size < 2) {
        flipper_auth_set_status(app, FlipperAuthViewStateApduError, "Keine Statusbytes", "");
        return;
    }

    const uint8_t sw1 = rx_data[rx_size - 2];
    const uint8_t sw2 = rx_data[rx_size - 1];
    if((sw1 != 0x90) || (sw2 != 0x00)) {
        char detail[AUTH_DISPLAY_DETAIL_SIZE];
        snprintf(detail, sizeof(detail), "Status %02X%02X", sw1, sw2);
        flipper_auth_set_status(app, FlipperAuthViewStateApduError, detail, "");
        return;
    }

    char payload[AUTH_DISPLAY_TEXT_SIZE];
    const size_t payload_size = rx_size - 2;
    const size_t copy_size = MIN(payload_size, sizeof(payload) - 1);

    memcpy(payload, rx_data, copy_size);
    payload[copy_size] = '\0';

    for(size_t i = 0; i < copy_size; i++) {
        const uint8_t c = payload[i];
        if((c < 0x20) || (c > 0x7E)) {
            payload[i] = '.';
        }
    }

    if(strcmp(payload, "NO_ACTIVE_SESSION") == 0) {
        flipper_auth_set_status(app, FlipperAuthViewStateNoSession, "AID OK, keine Payload", payload);
    } else if(flipper_auth_payload_has_required_fields(payload)) {
        char session_id[AUTH_FIELD_SESSION_SIZE];
        char username[AUTH_FIELD_USERNAME_SIZE];
        char action[AUTH_FIELD_ACTION_SIZE];
        char challenge[AUTH_FIELD_CHALLENGE_SIZE];

        const bool payload_parsed =
            flipper_auth_extract_field(payload, "sessionId=", session_id, sizeof(session_id)) &&
            flipper_auth_extract_field(payload, "username=", username, sizeof(username)) &&
            flipper_auth_extract_field(payload, "action=", action, sizeof(action)) &&
            flipper_auth_extract_field(payload, "challenge=", challenge, sizeof(challenge)) &&
            flipper_auth_is_digits(session_id);

        if(payload_parsed) {
            flipper_auth_set_payload_read(app, payload, session_id, username, action);
        } else {
            flipper_auth_set_status(
                app, FlipperAuthViewStateFormatError, "AID OK, sessionId fehlt", payload);
        }
    } else {
        flipper_auth_set_status(
            app, FlipperAuthViewStateFormatError, "AID OK, anderes Format", payload);
    }
}

static bool flipper_auth_send_pending_uart(FlipperAuthApp* app) {
    char session_id[AUTH_FIELD_SESSION_SIZE];

    furi_mutex_acquire(app->state_mutex, FuriWaitForever);
    const bool should_send = app->uart_send_pending && app->session_id[0] != '\0';
    snprintf(session_id, sizeof(session_id), "%s", app->session_id);
    furi_mutex_release(app->state_mutex);

    if(!should_send) {
        return false;
    }

    if(!app->serial_handle) {
        FURI_LOG_E(TAG, "UART handle not available");
        furi_mutex_acquire(app->state_mutex, FuriWaitForever);
        app->uart_send_pending = false;
        app->uart_sent = false;
        furi_mutex_release(app->state_mutex);
        return false;
    }

    char uart_line[AUTH_UART_LINE_SIZE];
    const int uart_line_size = snprintf(uart_line, sizeof(uart_line), "SESSION_ID:%s\n", session_id);
    if((uart_line_size <= 0) || ((size_t)uart_line_size >= sizeof(uart_line))) {
        FURI_LOG_E(TAG, "Could not format UART line");
        return false;
    }

    furi_hal_serial_tx(app->serial_handle, (const uint8_t*)uart_line, (size_t)uart_line_size);
    furi_hal_serial_tx_wait_complete(app->serial_handle);

    furi_mutex_acquire(app->state_mutex, FuriWaitForever);
    app->uart_send_pending = false;
    app->uart_sent = true;
    furi_mutex_release(app->state_mutex);

    FURI_LOG_I(TAG, "Sent UART line: %s", uart_line);
    return true;
}

static NfcCommand flipper_auth_nfc_callback(NfcGenericEvent event, void* context) {
    furi_assert(context);
    FlipperAuthApp* app = context;

    NfcCommand command = NfcCommandContinue;

    if(event.protocol != NfcProtocolIso14443_4a) {
        return command;
    }

    Iso14443_4aPollerEvent* iso_event = event.event_data;
    if(iso_event->type == Iso14443_4aPollerEventTypeReady) {
        bit_buffer_copy_bytes(
            app->tx_buffer, flipper_auth_select_apdu, sizeof(flipper_auth_select_apdu));
        bit_buffer_reset(app->rx_buffer);

        Iso14443_4aError iso_error =
            iso14443_4a_poller_send_block(event.instance, app->tx_buffer, app->rx_buffer);
        flipper_auth_parse_response(app, iso_error);

        FlipperAuthAppEvent app_event = {
            .type = FlipperAuthAppEventTypeNfcDone,
        };
        furi_message_queue_put(app->event_queue, &app_event, 0);

        command = NfcCommandStop;
    } else if(iso_event->type == Iso14443_4aPollerEventTypeError) {
        flipper_auth_set_status(app, FlipperAuthViewStateNfcError, "Karte nicht bereit", "");

        FlipperAuthAppEvent app_event = {
            .type = FlipperAuthAppEventTypeNfcDone,
        };
        furi_message_queue_put(app->event_queue, &app_event, 0);

        command = NfcCommandStop;
    }

    return command;
}

static void flipper_auth_stop_scan(FlipperAuthApp* app) {
    if(app->poller) {
        nfc_poller_stop(app->poller);
        nfc_poller_free(app->poller);
        app->poller = NULL;
    }
}

static void flipper_auth_start_scan(FlipperAuthApp* app) {
    flipper_auth_stop_scan(app);
    flipper_auth_set_status(app, FlipperAuthViewStateScanning, "AID SELECT wird gesendet", "");

    bit_buffer_reset(app->tx_buffer);
    bit_buffer_reset(app->rx_buffer);

    app->poller = nfc_poller_alloc(app->nfc, NfcProtocolIso14443_4a);
    nfc_poller_start(app->poller, flipper_auth_nfc_callback, app);
}

static FlipperAuthApp* flipper_auth_app_alloc(void) {
    FlipperAuthApp* app = malloc(sizeof(FlipperAuthApp));
    memset(app, 0, sizeof(FlipperAuthApp));

    app->event_queue = furi_message_queue_alloc(8, sizeof(FlipperAuthAppEvent));
    app->state_mutex = furi_mutex_alloc(FuriMutexTypeNormal);
    app->nfc = nfc_alloc();
    app->serial_handle = furi_hal_serial_control_acquire(FuriHalSerialIdUsart);
    if(app->serial_handle) {
        furi_hal_serial_init(app->serial_handle, AUTH_UART_BAUD_RATE);
    } else {
        FURI_LOG_E(TAG, "Could not acquire USART for WiFi Devboard UART");
    }
    app->tx_buffer = bit_buffer_alloc(AUTH_RX_BUFFER_SIZE);
    app->rx_buffer = bit_buffer_alloc(AUTH_RX_BUFFER_SIZE);

    app->view_state = FlipperAuthViewStateScanning;
    snprintf(app->detail, sizeof(app->detail), "%s", "AID SELECT wird gesendet");

    app->view_port = view_port_alloc();
    view_port_draw_callback_set(app->view_port, flipper_auth_draw_callback, app);
    view_port_input_callback_set(app->view_port, flipper_auth_input_callback, app->event_queue);

    app->gui = furi_record_open(RECORD_GUI);
    gui_add_view_port(app->gui, app->view_port, GuiLayerFullscreen);

    return app;
}

static void flipper_auth_app_free(FlipperAuthApp* app) {
    furi_assert(app);

    flipper_auth_stop_scan(app);

    gui_remove_view_port(app->gui, app->view_port);
    view_port_free(app->view_port);
    furi_record_close(RECORD_GUI);

    bit_buffer_free(app->rx_buffer);
    bit_buffer_free(app->tx_buffer);
    if(app->serial_handle) {
        furi_hal_serial_deinit(app->serial_handle);
        furi_hal_serial_control_release(app->serial_handle);
    }
    nfc_free(app->nfc);

    furi_mutex_free(app->state_mutex);
    furi_message_queue_free(app->event_queue);

    free(app);
}

int32_t flipper_auth_hce_reader_app(void* arg) {
    UNUSED(arg);

    FlipperAuthApp* app = flipper_auth_app_alloc();
    flipper_auth_start_scan(app);

    bool running = true;
    FlipperAuthAppEvent event;

    while(running) {
        if(furi_message_queue_get(app->event_queue, &event, FuriWaitForever) != FuriStatusOk) {
            continue;
        }

        if(event.type == FlipperAuthAppEventTypeInput) {
            if(event.input.type == InputTypeShort) {
                if(event.input.key == InputKeyBack) {
                    running = false;
                } else if(event.input.key == InputKeyOk) {
                    flipper_auth_start_scan(app);
                }
            }
        } else if(event.type == FlipperAuthAppEventTypeNfcDone) {
            flipper_auth_stop_scan(app);
            flipper_auth_send_pending_uart(app);
        }

        view_port_update(app->view_port);
    }

    flipper_auth_app_free(app);

    return 0;
}

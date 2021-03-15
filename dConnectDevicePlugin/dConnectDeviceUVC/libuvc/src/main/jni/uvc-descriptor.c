/*
 uvc-descriptor.c
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
#include <string.h>
#include <malloc.h>

#include "uvc-descriptor.h"

#define BYTE_TO_SHORT(x) ((x++) | ((x++) << 8))

#define BYTE_TO_INT(x) ((x++) | ((x++) << 8) | ((x++) << 16) | ((x++) << 24))

/**
 * 指定されたサイズでメモリを確保します。
 * <p>
 * 確保したメモリは全て 0 で初期化します。
 * </p>
 */
#define CALLOC(x) ((x*) calloc(1, sizeof(x)))

static const uint8_t GUID_YUY2_UNCOMPRESSED[] = {
        0x59, 0x55, 0x59, 0x32, 0x0, 0x0, 0x10, 0x0, 0x80, 0x0, 0x0, 0xAA, 0x0, 0x38, 0x9B, 0x71
};

static const uint8_t GUID_NV12_UNCOMPRESSED[] = {
        0x4E, 0x56, 0x31, 0x32, 0x0, 0x0, 0x10, 0x0, 0x80, 0x0, 0x0, 0xAA, 0x0, 0x38, 0x9B, 0x71
};

static const uint8_t GUID_M420_UNCOMPRESSED[] = {
        0x4D, 0x34, 0x32, 0x30, 0x0, 0x0, 0x10, 0x0, 0x80, 0x0, 0x0, 0xAA, 0x0, 0x38, 0x9B, 0x71
};

static const uint8_t GUID_I420_UNCOMPRESSED[] = {
        0x49, 0x34, 0x32, 0x30, 0x0, 0x0, 0x10, 0x0, 0x80, 0x0, 0x0, 0xAA, 0x0, 0x38, 0x9B, 0x71
};

static void uvc_add_configuration(struct uvc_descriptor *descriptor,
                                  struct uvc_configuration_descriptor *configuration) {
    if (configuration == NULL) {
        LOGE("uvc_add_configuration: configuration is NULL.");
        return;
    }

    if (descriptor->configuration == NULL) {
        descriptor->configuration = configuration;
    } else {
        struct uvc_configuration_descriptor *c = descriptor->configuration;
        while (c->next) {
            c = c->next;
        }
        c->next = configuration;
    }
}


static void uvc_add_video_streaming_interface(struct uvc_configuration_descriptor *configuration,
                                              struct uvc_video_streaming_interface *streaming_interface) {
    if (streaming_interface == NULL) {
        LOGE("uvc_add_video_streaming_interface: streaming_interface is NULL.");
        return;
    }

    if (configuration->streaming_interface == NULL) {
        configuration->streaming_interface = streaming_interface;
    } else {
        struct uvc_video_streaming_interface *s = configuration->streaming_interface;
        while (s->next) {
            s = s->next;
        }
        s->next = streaming_interface;
    }
}

static void uvc_add_vc_input_terminal(struct uvc_video_control_interface *control,
                                      struct uvc_vc_input_terminal_descriptor *input_terminal) {
    if (input_terminal == NULL) {
        LOGE("uvc_add_vc_input_terminal: input_terminal is NULL.");
        return;
    }

    if (control->input_terminal == NULL) {
        control->input_terminal = input_terminal;
    } else {
        struct uvc_vc_input_terminal_descriptor *p = control->input_terminal;
        while (p->next) {
            p = p->next;
        }
        p->next = input_terminal;
    }
}

static void uvc_add_vc_output_terminal(struct uvc_video_control_interface *control,
                                       struct uvc_vc_output_terminal_descriptor *output_terminal) {
    if (output_terminal == NULL) {
        LOGE("uvc_add_vc_output_terminal: output_terminal is NULL.");
        return;
    }

    if (control->output_terminal == NULL) {
        control->output_terminal = output_terminal;
    } else {
        struct uvc_vc_output_terminal_descriptor *p = control->output_terminal;
        while (p->next) {
            p = p->next;
        }
        p->next = output_terminal;
    }
}


static void uvc_add_vc_selector_unit(struct uvc_video_control_interface *control,
                                     struct uvc_vc_selector_unit_descriptor *selector) {
    if (selector == NULL) {
        LOGE("uvc_add_vc_selector_unit: selector is NULL.");
        return;
    }

    if (control->selector == NULL) {
        control->selector = selector;
    } else {
        struct uvc_vc_selector_unit_descriptor *p = control->selector;
        while (p->next) {
            p = p->next;
        }
        p->next = selector;
    }
}

static void uvc_add_vc_processing_unit(struct uvc_video_control_interface *control,
                                       struct uvc_vc_processing_unit_descriptor *processing) {
    if (processing == NULL) {
        LOGE("uvc_add_vc_processing_unit: processing is NULL.");
        return;
    }

    if (control->processing == NULL) {
        control->processing = processing;
    } else {
        struct uvc_vc_processing_unit_descriptor *p = control->processing;
        while (p->next) {
            p = p->next;
        }
        p->next = processing;
    }
}

static void uvc_add_vc_extension_unit(struct uvc_video_control_interface *control,
                                      struct uvc_vc_extension_unit_descriptor *extension) {
    if (extension == NULL) {
        LOGE("uvc_add_vc_extension_unit: extension is NULL.");
        return;
    }

    if (control->extension == NULL) {
        control->extension = extension;
    } else {
        struct uvc_vc_extension_unit_descriptor *e = control->extension;
        while (e->next) {
            e = e->next;
        }
        e->next = extension;
    }
}


static void uvc_add_vc_encoding_unit(struct uvc_video_control_interface *control,
                                     struct uvc_vc_encoding_unit_descriptor *encoding) {
    if (encoding == NULL) {
        LOGE("uvc_add_vc_encoding_unit: encoding is NULL.");
        return;
    }

    if (control->encoding == NULL) {
        control->encoding = encoding;
    } else {
        struct uvc_vc_encoding_unit_descriptor *e = control->encoding;
        while (e->next) {
            e = e->next;
        }
        e->next = encoding;
    }
}


static void uvc_add_vs_format(struct uvc_video_streaming_interface *stream,
                              struct uvc_vs_format_descriptor *format) {
    if (format == NULL) {
        LOGE("uvc_add_vs_format: format is NULL.");
        return;
    }

    if (stream->format == NULL) {
        stream->format = format;
    } else {
        struct uvc_vs_format_descriptor *e = stream->format;
        while (e->next) {
            e = e->next;
        }
        e->next = format;
    }
}


static void uvc_add_vs_frame(struct uvc_vs_format_descriptor *format,
                             struct uvc_vs_frame_descriptor *frame) {
    if (format == NULL) {
        LOGE("uvc_add_vs_frame: format is NULL.");
        return;
    }

    if (format->frame == NULL) {
        format->frame = frame;
    } else {
        struct uvc_vs_frame_descriptor *e = format->frame;
        while (e->next) {
            e = e->next;
        }
        e->next = frame;
    }
}

static void uvc_add_uvc_streaming_altsetting(struct uvc_configuration_descriptor *configuration,
                                             struct uvc_video_streaming_altsetting *altsetting) {
    if (configuration == NULL) {
        return;
    }
    if (altsetting == NULL) {
        return;
    }

    if (configuration->altsetting == NULL) {
        configuration->altsetting = altsetting;
    } else {
        struct uvc_video_streaming_altsetting *a = configuration->altsetting;
        while (a->next) {
            a = a->next;
        }
        a->next = altsetting;
    }
}

static void uvc_add_vs_color_matching(struct uvc_video_streaming_interface *stream,
                                      struct uvc_vs_color_matching_descriptor *color) {
    if (stream == NULL) {
        return;
    }

    if (stream->color_matching == NULL) {
        stream->color_matching = color;
    } else {
        struct uvc_vs_color_matching_descriptor *c = stream->color_matching;
        while (c->next) {
            c = c->next;
        }
        c->next = color;
    }
}

static struct uvc_vs_format_descriptor *uvc_get_last_format(struct uvc_video_streaming_interface *streaming) {
    if (streaming->format) {
        struct uvc_vs_format_descriptor *s = streaming->format;
        while (s->next) {
            s = s->next;
        }
        return s;
    }
    return NULL;
}

static struct uvc_device_descriptor *uvc_parse_device_descriptor(uint8_t *buffer) {
    struct uvc_device_descriptor *device = CALLOC(struct uvc_device_descriptor);
    if (device == NULL) {
        LOGE("uvc_parse_device_descriptor: Out of memory.");
        return NULL;
    }

    device->bLength = *buffer++;
    device->bDescriptorType = *buffer++;
    device->bcdUSB = BYTE_TO_SHORT(*buffer);
    device->bDeviceClass = *buffer++;
    device->bDeviceSubClass = *buffer++;
    device->bDeviceProtocol = *buffer++;
    device->bMaxPacketSize0 = *buffer++;
    device->idVendor = BYTE_TO_SHORT(*buffer);
    device->idProduct = BYTE_TO_SHORT(*buffer);
    device->bcdDevice = BYTE_TO_SHORT(*buffer);
    device->iManufacturer = *buffer++;
    device->iProduct = *buffer++;
    device->iSerialNumber = *buffer++;
    device->bNumConfigurations = *buffer;
    return device;
}

static struct uvc_configuration_descriptor *uvc_parse_configuration_descriptor(uint8_t *buffer) {
    struct uvc_configuration_descriptor *config = CALLOC(struct uvc_configuration_descriptor);
    if (config == NULL) {
        LOGE("uvc_parse_configuration_descriptor: Out of memory.");
        return NULL;
    }

    config->bLength = *buffer++;
    config->bDescriptorType = *buffer++;
    config->wTotalLength = BYTE_TO_SHORT(*buffer);
    config->bNumInterfaces = *buffer++;
    config->bConfigurationValue = *buffer++;
    config->iConfiguration = *buffer++;
    config->bmAttributes = *buffer++;
    config->bMaxPower = *buffer;

    return config;
}

static struct uvc_interface_descriptor *uvc_parse_interface_descriptor(uint8_t *buffer) {
    struct uvc_interface_descriptor *interface = CALLOC(struct uvc_interface_descriptor);
    if (interface == NULL) {
        LOGE("uvc_parse_interface_descriptor: Out of memory.");
        return NULL;
    }

    interface->bLength = *buffer++;
    interface->bDescriptorType = *buffer++;
    interface->bInterfaceNumber = *buffer++;
    interface->bAlternateSetting = *buffer++;
    interface->bNumEndpoints = *buffer++;
    interface->bInterfaceClass = *buffer++;
    interface->bInterfaceSubClass = *buffer++;
    interface->bInterfaceProtocol = *buffer++;
    interface->iInterface = *buffer;

    return interface;
}

static struct uvc_endpoint_descriptor *uvc_parse_endpoint(uint8_t *buffer) {
    struct uvc_endpoint_descriptor *endpoint = CALLOC(struct uvc_endpoint_descriptor);
    if (endpoint == NULL) {
        LOGE("uvc_endpoint_descriptor: Out of memory.");
        return NULL;
    }

    endpoint->bLength = *buffer++;
    endpoint->bDescriptorType = *buffer++;
    endpoint->bEndpointAddress = *buffer++;
    endpoint->bmAttributes = *buffer++;
    endpoint->wMaxPacketSize = BYTE_TO_SHORT(*buffer);
    endpoint->bInterval = *buffer;

    return endpoint;
}

static struct uvc_interrupt_endpoint_descriptor * uvc_parse_interrupt_endpoint(uint8_t *buffer) {
    struct uvc_interrupt_endpoint_descriptor *endpoint = CALLOC(struct uvc_interrupt_endpoint_descriptor);
    if (endpoint == NULL) {
        LOGE("uvc_parse_interrupt_endpoint: Out of memory.");
        return NULL;
    }

    endpoint->bLength = *buffer++;
    endpoint->bDescriptorType = *buffer++;
    endpoint->bDescriptorSubType = *buffer++;
    endpoint->wMaxTransferSize = BYTE_TO_SHORT(*buffer);
    return endpoint;
}

static struct uvc_interface_association_descriptor *uvc_parse_iad(uint8_t *buffer) {
    struct uvc_interface_association_descriptor *iad = CALLOC(struct uvc_interface_association_descriptor);
    if (iad == NULL) {
        LOGE("uvc_interface_association_descriptor: Out of memory.");
        return NULL;
    }

    iad->bLength = *buffer++;
    iad->bDescriptorType = *buffer++;
    iad->bFirstInterface = *buffer++;
    iad->bInterfaceCount = *buffer++;
    iad->bFunctionClass = *buffer++;
    iad->bFunctionSubClass = *buffer++;
    iad->bFunctionProtocol = *buffer++;
    iad->iFunction = *buffer;
    return iad;
}

static void uvc_free_vc_header(struct uvc_vc_header_descriptor *header) {
    if (header) {
        SAFE_FREE(header->baInterfaceNr);
        SAFE_FREE(header);
    }
}

static struct uvc_vc_header_descriptor *uvc_parse_vc_header(uint8_t *buffer) {
    struct uvc_vc_header_descriptor *header = CALLOC(struct uvc_vc_header_descriptor);
    if (header == NULL) {
        LOGE("uvc_vc_header_descriptor: Out of memory.");
        return NULL;
    }

    header->bLength = *buffer++;
    header->bDescriptorType = *buffer++;
    header->bDescriptorSubType = *buffer++;
    header->bcdUVC = BYTE_TO_SHORT(*buffer);
    header->wTotalLength = BYTE_TO_SHORT(*buffer);
    header->dwClockFrequency = BYTE_TO_INT(*buffer);
    header->bInCollection = *buffer++;
    header->baInterfaceNr = (uint8_t *) calloc(1, header->bInCollection);
    if (header->baInterfaceNr == NULL) {
        LOGE("uvc_vc_header_descriptor: Out of memory.");
        uvc_free_vc_header(header);
        return NULL;
    }

    for (int i = 0; i < header->bInCollection; i++) {
        header->baInterfaceNr[i] = *buffer++;
    }

    return header;
}

static void uvc_free_vc_input_terminal(struct uvc_vc_input_terminal_descriptor *input_terminal) {
    if (input_terminal) {
        uvc_free_vc_input_terminal(input_terminal->next);
        SAFE_FREE(input_terminal->bmControls);
        SAFE_FREE(input_terminal->bmTransportModes);
        SAFE_FREE(input_terminal);
    }
}

static struct uvc_vc_input_terminal_descriptor *uvc_parse_vc_input_terminal(uint8_t *buffer) {
    struct uvc_vc_input_terminal_descriptor *input_terminal = CALLOC(struct uvc_vc_input_terminal_descriptor);
    if (input_terminal == NULL) {
        LOGE("uvc_vc_input_terminal_descriptor: Out of memory.");
        return NULL;
    }

    input_terminal->bLength = *buffer++;
    input_terminal->bDescriptorType = *buffer++;
    input_terminal->bDescriptorSubType = *buffer++;
    input_terminal->bTerminalID = *buffer++;
    input_terminal->wTerminalType = BYTE_TO_SHORT(*buffer);
    input_terminal->bAssocTerminal = *buffer++;
    input_terminal->iTerminal = *buffer++;

    if (input_terminal->wTerminalType == ITT_CAMERA) {
        input_terminal->wObjectiveFocalLengthMin = BYTE_TO_SHORT(*buffer);
        input_terminal->wObjectiveFocalLengthMax = BYTE_TO_SHORT(*buffer);
        input_terminal->wOcularFocalLength =  BYTE_TO_SHORT(*buffer);
    }

    input_terminal->bControlSize = *buffer;
    input_terminal->bmControls = (uint8_t *) calloc(1, input_terminal->bControlSize);
    if (input_terminal->bmControls == NULL) {
        LOGE("uvc_vc_input_terminal_descriptor: Out of memory.");
        uvc_free_vc_input_terminal(input_terminal);
        return NULL;
    }

    for (int i = 0; i < input_terminal->bControlSize; i++) {
        input_terminal->bmControls[i] = *buffer++;
    }

    if (input_terminal->wTerminalType == ITT_MEDIA_TRANSPORT_INPUT) {
        input_terminal->bTransportModeSize = *buffer++;
        input_terminal->bmTransportModes = (uint8_t *) calloc(1, input_terminal->bTransportModeSize);
        if (input_terminal->bmTransportModes == NULL) {
            LOGE("uvc_vc_input_terminal_descriptor: Out of memory.");
            uvc_free_vc_input_terminal(input_terminal);
            return NULL;
        }

        for (int i = 0; i < input_terminal->bTransportModeSize; i++) {
            input_terminal->bmTransportModes[i] = *buffer++;
        }
    }

    return input_terminal;
}

static void uvc_free_vc_output_terminal(struct uvc_vc_output_terminal_descriptor *output_terminal) {
    if (output_terminal) {
        uvc_free_vc_output_terminal(output_terminal->next);
        SAFE_FREE(output_terminal);
    }
}

static struct uvc_vc_output_terminal_descriptor *uvc_parse_vc_output_terminal(uint8_t *buffer) {
    struct uvc_vc_output_terminal_descriptor *output_terminal = CALLOC(struct uvc_vc_output_terminal_descriptor);
    if (output_terminal == NULL) {
        LOGE("uvc_vc_output_terminal_descriptor: Out of memory.");
        return NULL;
    }

    output_terminal->bLength = *buffer++;
    output_terminal->bDescriptorType = *buffer++;
    output_terminal->bDescriptorSubType = *buffer++;
    output_terminal->bTerminalID = *buffer++;
    output_terminal->wTerminalType = BYTE_TO_SHORT(*buffer);
    output_terminal->bAssocTerminal = *buffer++;
    output_terminal->bSourceID = *buffer++;
    output_terminal->iTerminal = *buffer;

    return output_terminal;
}

static void uvc_free_vc_processing_unit(struct uvc_vc_processing_unit_descriptor *processing) {
    if (processing) {
        uvc_free_vc_processing_unit(processing->next);
        SAFE_FREE(processing->bmControls);
        SAFE_FREE(processing);
    }
}

static struct uvc_vc_processing_unit_descriptor *uvc_parse_vc_processing_unit(uint8_t *buffer) {
    struct uvc_vc_processing_unit_descriptor *processing =CALLOC(struct uvc_vc_processing_unit_descriptor);
    if (processing == NULL) {
        LOGE("uvc_vc_processing_unit_descriptor: Out of memory.");
        return NULL;
    }

    processing->bLength = *buffer++;
    processing->bDescriptorType = *buffer++;
    processing->bDescriptorSubType = *buffer++;
    processing->bUnitID = *buffer++;
    processing->bSourceID = *buffer++;
    processing->wMaxMultiplier = BYTE_TO_SHORT(*buffer);
    processing->bControlSize = *buffer++;
    processing->bmControls = (uint8_t *) calloc(1, processing->bControlSize);
    if (processing->bmControls == NULL) {
        LOGE("uvc_vc_processing_unit_descriptor: Out of memory.");
        uvc_free_vc_processing_unit(processing);
        return NULL;
    }

    for (int i = 0; i < processing->bControlSize; i++) {
        processing->bmControls[i] = *buffer++;
    }
    processing->iProcessing = *buffer++;
    processing->bmVideoStandards = *buffer;

    return processing;
}

static void uvc_free_vc_extension_unit(struct uvc_vc_extension_unit_descriptor *extension) {
    if (extension) {
        uvc_free_vc_extension_unit(extension->next);
        SAFE_FREE(extension->baSourceID);
        SAFE_FREE(extension->bmControls);
        SAFE_FREE(extension);
    }
}

static struct uvc_vc_extension_unit_descriptor *uvc_parse_vc_extension_unit(uint8_t *buffer) {
    struct uvc_vc_extension_unit_descriptor *extension = CALLOC(struct uvc_vc_extension_unit_descriptor);
    if (extension == NULL) {
        LOGE("uvc_vc_extension_unit_descriptor: Out of memory.");
        return NULL;
    }

    extension->bLength = *buffer++;
    extension->bDescriptorType = *buffer++;
    extension->bDescriptorSubType = *buffer++;
    extension->bUnitID = *buffer++;
    for (int i = 0; i < 16; i++) {
        extension->guidExtensionCode[i] = *buffer++;
    }
    extension->bNumControls = *buffer++;
    extension->bNrInPins = *buffer++;
    extension->baSourceID = (uint8_t *) calloc(1, extension->bNrInPins);
    if (extension->baSourceID == NULL) {
        LOGE("uvc_vc_extension_unit_descriptor: Out of memory.");
        uvc_free_vc_extension_unit(extension);
        return NULL;
    }

    for (int i = 0; i < extension->bNrInPins; i++) {
        extension->baSourceID[i] = *buffer++;
    }


    extension->bControlSize = *buffer++;
    extension->bmControls = (uint8_t *) calloc(1, extension->bControlSize);
    if (extension->bmControls == NULL) {
        LOGE("uvc_vc_extension_unit_descriptor: Out of memory.");
        uvc_free_vc_extension_unit(extension);
        return NULL;
    }

    for (int i = 0; i < extension->bControlSize; i++) {
        extension->bmControls[i] = *buffer++;
    }
    extension->iExtension = *buffer;

    return extension;
}

static void uvc_free_vc_selector_unit(struct uvc_vc_selector_unit_descriptor *selector) {
    if (selector) {
        uvc_free_vc_selector_unit(selector->next);
        SAFE_FREE(selector->baSourceID);
        SAFE_FREE(selector);
    }
}

static struct uvc_vc_selector_unit_descriptor *uvc_parse_vc_selector_unit(uint8_t *buffer) {
    struct uvc_vc_selector_unit_descriptor *selector = CALLOC(struct uvc_vc_selector_unit_descriptor);
    if (selector == NULL) {
        LOGE("uvc_vc_selector_unit_descriptor: Out of memory.");
        return NULL;
    }

    selector->bLength = *buffer++;
    selector->bDescriptorType = *buffer++;
    selector->bDescriptorSubType = *buffer++;
    selector->bUnitID = *buffer++;
    selector->bNrInPins = *buffer++;
    selector->baSourceID = (uint8_t *) calloc(1, selector->bNrInPins);
    if (selector->baSourceID == NULL) {
        LOGE("uvc_vc_selector_unit_descriptor: Out of memory.");
        uvc_free_vc_selector_unit(selector);
        return NULL;
    }

    for (int i = 0; i < selector->bNrInPins; i++) {
        selector->baSourceID[i] = *buffer++;
    }
    selector->iSelector = *buffer;

    return selector;
}

static void uvc_free_vc_encoding_unit(struct uvc_vc_encoding_unit_descriptor *encoding) {
    if (encoding) {
        uvc_free_vc_encoding_unit(encoding->next);
        SAFE_FREE(encoding->bmControls);
        SAFE_FREE(encoding->bmControlsRuntime);
        SAFE_FREE(encoding);
    }
}

static struct uvc_vc_encoding_unit_descriptor *uvc_parse_vc_encoding_unit(uint8_t *buffer) {
    struct uvc_vc_encoding_unit_descriptor *encoding = CALLOC(struct uvc_vc_encoding_unit_descriptor);
    if (encoding == NULL) {
        LOGE("uvc_vc_encoding_unit_descriptor: Out of memory.");
        return NULL;
    }

    encoding->bLength = *buffer++;
    encoding->bDescriptorType = *buffer++;
    encoding->bDescriptorSubType = *buffer++;
    encoding->bUnitID = *buffer++;
    encoding->bSourceID = *buffer++;
    encoding->iEncoding = *buffer++;
    encoding->bControlSize = *buffer++;
    encoding->bmControls = (uint8_t *) calloc(1, encoding->bControlSize);
    if (encoding->bmControls == NULL) {
        uvc_free_vc_encoding_unit(encoding);
        return NULL;
    }

    for (int i = 0; i < encoding->bControlSize; i++) {
        encoding->bmControls[i] = *buffer++;
    }

    encoding->bmControlsRuntime = (uint8_t *) calloc(1, encoding->bControlSize);
    if (encoding->bmControlsRuntime == NULL) {
        uvc_free_vc_encoding_unit(encoding);
        return NULL;
    }

    for (int i = 0; i < encoding->bControlSize; i++) {
        encoding->bmControlsRuntime[i] = *buffer++;
    }

    return encoding;
}

/////////


static void uvc_free_vs_header(struct uvc_vs_header_descriptor *header) {
    if (header) {
        SAFE_FREE(header->bmaControls);
        SAFE_FREE(header);
    }
}

static struct uvc_vs_header_descriptor *uvc_parse_vs_header(uint8_t *buffer) {
    struct uvc_vs_header_descriptor *header = CALLOC(struct uvc_vs_header_descriptor);
    if (header == NULL) {
        LOGE("uvc_parse_vs_header: Out of memory.");
        return NULL;
    }

    header->bLength = *buffer++;
    header->bDescriptorType = *buffer++;
    header->bDescriptorSubType = *buffer++;
    header->bNumFormats = *buffer++;
    header->wTotalLength = BYTE_TO_SHORT(*buffer);
    header->bEndpointAddress = *buffer++;
    header->bmInfo = *buffer++;
    header->bTerminalLink = *buffer++;
    header->bStillCaptureMethod = *buffer++;
    header->bTriggerSupport = *buffer++;
    header->bTriggerUsage = *buffer++;
    header->bControlSize = *buffer++;
    header->bmaControls = (uint8_t *) calloc(1, header->bControlSize);
    if (header->bmaControls == NULL) {
        LOGE("uvc_parse_vs_header: Out of memory.");
        uvc_free_vs_header(header);
        return NULL;
    }

    for (int i = 0; i < header->bControlSize; i++) {
        header->bmaControls[i] = *buffer++;
    }

    return header;
}

static void uvc_free_vs_frame_mjpeg(struct uvc_vs_frame_mjpeg_descriptor *frame) {
    if (frame) {
        SAFE_FREE(frame->dwFrameInterval);
        SAFE_FREE(frame);
    }
}

static struct uvc_vs_frame_descriptor *uvc_parse_vs_frame_mjpeg(uint8_t *buffer) {
    struct uvc_vs_frame_mjpeg_descriptor *frame = CALLOC(struct uvc_vs_frame_mjpeg_descriptor);
    if (frame == NULL) {
        LOGE("uvc_parse_vs_frame_mjpeg: Out of memory.");
        return NULL;
    }

    frame->bLength = *buffer++;
    frame->bDescriptorType = *buffer++;
    frame->bDescriptorSubType = *buffer++;
    frame->bFrameIndex = *buffer++;
    frame->bmCapabilities = *buffer++;
    frame->wWidth = BYTE_TO_SHORT(*buffer);
    frame->wHeight = BYTE_TO_SHORT(*buffer);
    frame->dwMinBitRate = BYTE_TO_INT(*buffer);
    frame->dwMaxBitRate = BYTE_TO_INT(*buffer);
    frame->dwMaxVideoFrameBufferSize = BYTE_TO_INT(*buffer);
    frame->dwDefaultFrameInterval = BYTE_TO_INT(*buffer);
    frame->bFrameIntervalType = *buffer++;
    if (frame->bFrameIntervalType == 0) {
        frame->dwMinFrameInterval = BYTE_TO_INT(*buffer);
        frame->dwMaxFrameInterval = BYTE_TO_INT(*buffer);
        frame->dwFrameIntervalStep = BYTE_TO_INT(*buffer);
    } else {
        frame->dwFrameInterval = (uint32_t *) calloc(frame->bFrameIntervalType, sizeof(uint32_t));
        if (frame->dwFrameInterval == NULL) {
            LOGE("uvc_parse_vs_frame_mjpeg: Out of memory.");
            uvc_free_vs_frame_mjpeg(frame);
            return NULL;
        }
        for (int i = 0; i < frame->bFrameIntervalType; i++) {
            frame->dwFrameInterval[i] = BYTE_TO_INT(*buffer);
        }
    }

    return (struct uvc_vs_frame_descriptor *) frame;
}

static struct uvc_vs_format_descriptor *uvc_parse_vs_format_mjpeg(uint8_t *buffer) {
    struct uvc_vs_format_mjpeg_descriptor *format = CALLOC(struct uvc_vs_format_mjpeg_descriptor);
    if (format == NULL) {
        LOGE("uvc_parse_vs_format_mjpeg: Out of memory.");
        return NULL;
    }

    format->bLength = *buffer++;
    format->bDescriptorType = *buffer++;
    format->bDescriptorSubType = *buffer++;
    format->bFormatIndex = *buffer++;
    format->bNumFrameDescriptors = *buffer++;
    format->bmFlags = *buffer++;
    format->bDefaultFrameIndex = *buffer++;
    format->bAspectRatioX = *buffer++;
    format->bAspectRatioY = *buffer++;
    format->bmInterlaceFlags = *buffer++;
    format->bCopyProtect = *buffer;

    return (struct uvc_vs_format_descriptor *) format;
}


static void uvc_free_vs_frame_uncompressed(struct uvc_vs_frame_uncompressed_descriptor *frame) {
    if (frame) {
        SAFE_FREE(frame->dwFrameInterval);
        SAFE_FREE(frame);
    }
}

static struct uvc_vs_frame_descriptor *uvc_vs_frame_uncompressed(uint8_t *buffer) {
    struct uvc_vs_frame_uncompressed_descriptor *frame = CALLOC(struct uvc_vs_frame_uncompressed_descriptor);
    if (frame == NULL) {
        LOGE("uvc_vs_frame_uncompressed: Out of memory.");
        return NULL;
    }

    frame->bLength = *buffer++;
    frame->bDescriptorType = *buffer++;
    frame->bDescriptorSubType = *buffer++;
    frame->bFrameIndex = *buffer++;
    frame->bmCapabilities = *buffer++;
    frame->wWidth = BYTE_TO_SHORT(*buffer);
    frame->wHeight = BYTE_TO_SHORT(*buffer);
    frame->dwMinBitRate = BYTE_TO_INT(*buffer);
    frame->dwMaxBitRate = BYTE_TO_INT(*buffer);
    frame->dwMaxVideoFrameBufferSize = BYTE_TO_INT(*buffer);
    frame->dwDefaultFrameInterval = BYTE_TO_INT(*buffer);
    frame->bFrameIntervalType = *buffer++;
    if (frame->bFrameIntervalType == 0) {
        frame->dwMinFrameInterval = BYTE_TO_INT(*buffer);
        frame->dwMaxFrameInterval = BYTE_TO_INT(*buffer);
        frame->dwFrameIntervalStep = BYTE_TO_INT(*buffer);
    } else {
        frame->dwFrameInterval = (uint32_t *) calloc(frame->bFrameIntervalType, sizeof(uint32_t));
        if (frame->dwFrameInterval == NULL) {
            uvc_free_vs_frame_uncompressed(frame);
            return NULL;
        }
        for (int i = 0; i < frame->bFrameIntervalType; i++) {
            frame->dwFrameInterval[i] = BYTE_TO_INT(*buffer);
        }
    }

    return (struct uvc_vs_frame_descriptor *) frame;
}

static struct uvc_vs_format_descriptor *uvc_vs_format_uncompressed(uint8_t *buffer) {
    struct uvc_vs_format_uncompressed_descriptor *format = CALLOC(struct uvc_vs_format_uncompressed_descriptor);
    if (format == NULL) {
        LOGE("uvc_vs_format_uncompressed: Out of memory.");
        return NULL;
    }

    format->bLength = *buffer++;
    format->bDescriptorType = *buffer++;
    format->bDescriptorSubType = *buffer++;
    format->bFormatIndex = *buffer++;
    format->bNumFrameDescriptors = *buffer++;
    for (int i = 0; i < 16; i++) {
        format->guidFormat[i] = *buffer++;
    }
    format->bBitsPerPixel = *buffer++;
    format->bDefaultFrameIndex = *buffer++;
    format->bAspectRatioX = *buffer++;
    format->bAspectRatioY = *buffer++;
    format->bmInterlaceFlags = *buffer++;
    format->bCopyProtect = *buffer;

    return (struct uvc_vs_format_descriptor *) format;
}


static struct uvc_vs_format_descriptor *uvc_parse_vs_format_h264(uint8_t *buffer) {
    struct uvc_vs_format_h264_descriptor *format = CALLOC(struct uvc_vs_format_h264_descriptor);
    if (format == NULL) {
        LOGE("uvc_parse_vs_format_h264: Out of memory.");
        return NULL;
    }

    format->bLength = *buffer++;
    format->bDescriptorType = *buffer++;
    format->bDescriptorSubType = *buffer++;
    format->bFormatIndex = *buffer++;
    format->bNumFrameDescriptors = *buffer++;
    format->bDefaultFrameIndex = *buffer++;
    format->bMaxCodecConfigDelay = *buffer++;
    format->bmSupportedSliceModes = *buffer++;
    format->bmSupportedSyncFrameTypes = *buffer++;
    format->bResuolutionScaling = *buffer++;
    format->Reserved1 = *buffer++;
    format->bmSupportedRateControlModes = *buffer++;
    format->wMaxMBperSecOneResolutionNoScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecTwoResolutionsNoScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecThreeResolutionsNoScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecFourResolutionsNoScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecOneResolutionTemporalScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecTwoResolutionsTemporalScalablility = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecThreeResolutionsTemporalScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecFourResolutionsTemporalScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecOneResolutionTemporalQualityScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecTwoResolutionsTemporalQualityScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecThreeResolutionsTemporalQualityScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecFourResolutionsTemporalQualityScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecOneResolutionsTemporalSpatialScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecTwoResolutionsTemporalSpatialScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecThreeResolutionsTemporalSpatialScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecFourResolutionsTemporalSpatialScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecOneResolutionFullScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecTwoResolutionsFullScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecThreeResolutionsFullScalability = BYTE_TO_SHORT(*buffer);
    format->wMaxMBperSecFourResolutionsFullScalability = BYTE_TO_SHORT(*buffer);

    return (struct uvc_vs_format_descriptor *) format;
}

static void uvc_free_vs_frame_h264(struct uvc_vs_frame_h264_descriptor *frame) {
    if (frame) {
        SAFE_FREE(frame->dwFrameInterval);
        SAFE_FREE(frame);
    }
}

static struct uvc_vs_frame_descriptor *uvc_parse_vs_frame_h264(uint8_t *buffer) {
    struct uvc_vs_frame_h264_descriptor *frame = CALLOC(struct uvc_vs_frame_h264_descriptor);
    if (frame == NULL) {
        LOGE("uvc_parse_vs_frame_h264: Out of memory.");
        return NULL;
    }

    frame->bLength = *buffer++;
    frame->bDescriptorType = *buffer++;
    frame->bDescriptorSubType = *buffer++;
    frame->bFrameIndex = *buffer++;
    frame->wWidth = BYTE_TO_SHORT(*buffer);
    frame->wHeight = BYTE_TO_SHORT(*buffer);
    frame->wSARwidth = BYTE_TO_SHORT(*buffer);
    frame->wSARheight = BYTE_TO_SHORT(*buffer);
    frame->wProfie = BYTE_TO_SHORT(*buffer);
    frame->bLevelIDC = *buffer++;
    frame->wConstrainedToolset = BYTE_TO_SHORT(*buffer);
    frame->bmSupportedUsages = BYTE_TO_INT(*buffer);
    frame->bmCapabilities = BYTE_TO_SHORT(*buffer);
    frame->bmSVCCapabilities = BYTE_TO_INT(*buffer);
    frame->bmMVCCapabilities = BYTE_TO_INT(*buffer);
    frame->dwMinBitRate = BYTE_TO_INT(*buffer);
    frame->dwMaxBitRate = BYTE_TO_INT(*buffer);
    frame->dwDefaultFrameInterval = BYTE_TO_INT(*buffer);
    frame->bNumFrameIntervals = *buffer++;
    frame->dwFrameInterval = (uint32_t *) calloc(frame->bNumFrameIntervals, sizeof(uint32_t));
    if (frame->dwFrameInterval == NULL) {
        LOGE("uvc_parse_vs_frame_h264: Out of memory.");
        uvc_free_vs_frame_h264(frame);
        return NULL;
    }

    for (int i = 0; i < frame->bNumFrameIntervals; i++) {
        frame->dwFrameInterval[i] = BYTE_TO_INT(*buffer);
    }

    return (struct uvc_vs_frame_descriptor *) frame;
}

static void uvc_free_vs_frame(struct uvc_vs_frame_descriptor *frame) {
    if (frame) {
        uvc_free_vs_frame(frame->next);

        // frame の中で個別にメモリを確保している場合があるので、処理を分ける
        switch (frame->bDescriptorSubType) {
            default:
                SAFE_FREE(frame);
                break;

            case VS_FRAME_UNCOMPRESSED:
                uvc_free_vs_frame_uncompressed((struct uvc_vs_frame_uncompressed_descriptor *) frame);
                break;

            case VS_FRAME_MJPEG:
                uvc_free_vs_frame_mjpeg((struct uvc_vs_frame_mjpeg_descriptor *) frame);
                break;

            case VS_FRAME_H264:
                uvc_free_vs_frame_h264((struct uvc_vs_frame_h264_descriptor *) frame);
                break;
        }
    }
}

static void uvc_free_vs_format(struct uvc_vs_format_descriptor *format) {
    if (format) {
        uvc_free_vs_format(format->next);
        uvc_free_vs_frame(format->frame);
        SAFE_FREE(format);
    }
}

static void uvc_free_vs_still_image_frame(struct uvc_vs_still_image_frame_descriptor *frame_still) {
    if (frame_still) {
        SAFE_FREE(frame_still->wWidth);
        SAFE_FREE(frame_still->wHeight);
        SAFE_FREE(frame_still->bCompression);
        SAFE_FREE(frame_still);
    }
}

static struct uvc_vs_still_image_frame_descriptor *uvc_parse_vs_still_image_frame(uint8_t *buffer) {
    struct uvc_vs_still_image_frame_descriptor *frame_still = CALLOC(struct uvc_vs_still_image_frame_descriptor);
    if (frame_still == NULL) {
        LOGE("uvc_parse_vs_still_image_frame: Out of memory.");
        return NULL;
    }

    frame_still->bLength = *buffer++;
    frame_still->bDescriptorType = *buffer++;
    frame_still->bDescriptorSubType = *buffer++;
    frame_still->bEndpointAddress = *buffer++;
    frame_still->bNumImageSizePatterns = *buffer++;

    size_t size = (size_t) 2 * frame_still->bNumImageSizePatterns;
    frame_still->wWidth = (uint16_t *) calloc(1, size);
    if (frame_still->wWidth == NULL) {
        LOGE("uvc_parse_vs_still_image_frame: Out of memory.");
        uvc_free_vs_still_image_frame(frame_still);
        return NULL;
    }

    frame_still->wHeight = (uint16_t *) calloc(1, size);
    if (frame_still->wHeight == NULL) {
        LOGE("uvc_parse_vs_still_image_frame: Out of memory.");
        uvc_free_vs_still_image_frame(frame_still);
        return NULL;
    }

    for (int i = 0; i < frame_still->bNumImageSizePatterns; i++) {
        frame_still->wWidth[i] = BYTE_TO_SHORT(*buffer);
        frame_still->wHeight[i] = BYTE_TO_SHORT(*buffer);
    }
    frame_still->bNumCompressionPtn = *buffer++;
    frame_still->bCompression = (uint8_t *) calloc(1, frame_still->bNumCompressionPtn);
    if (frame_still->bCompression == NULL) {
        LOGE("uvc_parse_vs_still_image_frame: Out of memory.");
        uvc_free_vs_still_image_frame(frame_still);
        return NULL;
    }

    for (int i = 0; i < frame_still->bNumCompressionPtn; i++) {
        frame_still->bCompression[i] = *buffer;
    }

    return frame_still;
}

static struct uvc_vs_color_matching_descriptor *uvc_parse_vs_color_matching(uint8_t *buffer) {
    struct uvc_vs_color_matching_descriptor *color = CALLOC(struct uvc_vs_color_matching_descriptor);
    if (color == NULL) {
        LOGE("uvc_parse_vs_color_matching: Out of memory.");
        return NULL;
    }

    color->bLength = *buffer++;
    color->bDescriptorType = *buffer++;
    color->bDescriptorSubType = *buffer++;
    color->bColorPrimaries = *buffer++;
    color->bTransferCharacteristics = *buffer++;
    color->bMatrixCoefficients = *buffer;

    return color;
}

static void uvc_free_vs_color_matching(struct uvc_vs_color_matching_descriptor *color) {
    if (color) {
        uvc_free_vs_color_matching(color->next);
        SAFE_FREE(color);
    }
}

static uint32_t uvc_parse_vc_descriptor(struct uvc_video_control_interface *control, uint8_t *buffer, int32_t length) {
    uint32_t skipLength = 0;
    while (length > 0) {
        uint8_t bLength = *buffer;
        uint8_t bDescriptorType = *(buffer + 1);

        switch (bDescriptorType) {
            case INTERFACE:
                // 次の Interface がきたので終了
                return skipLength;

            case ENDPOINT:
                control->endpoint = uvc_parse_endpoint(buffer);
                break;

            case CS_ENDPOINT:
                control->interrupt_endpoint = uvc_parse_interrupt_endpoint(buffer);
                break;

            case CS_INTERFACE: {
                uint8_t bDescriptorSubtype = *(buffer + 2);
                switch (bDescriptorSubtype) {
                    case VC_HEADER:
                        control->header = uvc_parse_vc_header(buffer);
                        break;

                    case VC_INPUT_TERMINAL:
                        uvc_add_vc_input_terminal(control, uvc_parse_vc_input_terminal(buffer));
                        break;

                    case VC_OUTPUT_TERMINAL:
                        uvc_add_vc_output_terminal(control, uvc_parse_vc_output_terminal(buffer));
                        break;

                    case VC_SELECTOR_UNIT:
                        uvc_add_vc_selector_unit(control, uvc_parse_vc_selector_unit(buffer));
                        break;

                    case VC_PROCESSING_UNIT:
                        uvc_add_vc_processing_unit(control, uvc_parse_vc_processing_unit(buffer));
                        break;

                    case VC_EXTENSION_UNIT:
                        uvc_add_vc_extension_unit(control, uvc_parse_vc_extension_unit(buffer));
                        break;

                    case VC_ENCODING_UNIT:
                        uvc_add_vc_encoding_unit(control, uvc_parse_vc_encoding_unit(buffer));
                        break;

                    default:
                        LOGW("UNKNOWN CS INTERFACE (VC). %02X", bDescriptorSubtype);
                        break;
                }
            }   break;

            default:
                LOGW("UNKNOWN DESCRIPTOR (VC). %02X", bDescriptorType);
                break;
        }

        buffer += bLength;
        skipLength += bLength;
        length -= bLength;
    }

    return skipLength;
}


static void uvc_free_video_control(struct uvc_video_control_interface *control_interface) {
    if (control_interface) {
        SAFE_FREE(control_interface->interface);

        uvc_free_vc_header(control_interface->header);
        uvc_free_vc_input_terminal(control_interface->input_terminal);
        uvc_free_vc_output_terminal(control_interface->output_terminal);
        uvc_free_vc_selector_unit(control_interface->selector);
        uvc_free_vc_processing_unit(control_interface->processing);
        uvc_free_vc_extension_unit(control_interface->extension);
        uvc_free_vc_encoding_unit(control_interface->encoding);

        SAFE_FREE(control_interface->endpoint);
        SAFE_FREE(control_interface->interrupt_endpoint);
        SAFE_FREE(control_interface);
    }
}


static uint32_t uvc_parse_vs_descriptor(struct uvc_video_streaming_interface *streaming, uint8_t *buffer, int32_t length) {
    uint32_t skipLength = 0;
    while (length > 0) {
        uint8_t bLength = *buffer;
        uint8_t bDescriptorType = *(buffer + 1);

        switch (bDescriptorType) {
            case INTERFACE:
                // 次の Interface がきたので終了
                return skipLength;

            case ENDPOINT:
                streaming->still_endpoint = uvc_parse_endpoint(buffer);
                break;

            case CS_INTERFACE: {
                uint8_t bDescriptorSubtype = *(buffer + 2);
                switch (bDescriptorSubtype) {
                    case VS_INPUT_HEADER:
                        streaming->header = uvc_parse_vs_header(buffer);
                        break;

                    case VS_OUTPUT_HEADER:
                        LOGW("VS_OUTPUT_HEADER is not implements yet.");
                        break;

                    case VS_FORMAT_MJPEG: {
                        struct uvc_vs_format_descriptor *format = uvc_parse_vs_format_mjpeg(buffer);
                        if (format) {
                            format->streaming_interface = streaming;
                            uvc_add_vs_format(streaming, format);
                        }
                    }   break;

                    case VS_FRAME_MJPEG:
                        uvc_add_vs_frame(uvc_get_last_format(streaming), uvc_parse_vs_frame_mjpeg(buffer));
                        break;

                    case VS_FORMAT_UNCOMPRESSED: {
                        struct uvc_vs_format_descriptor *format = uvc_vs_format_uncompressed(buffer);
                        if (format) {
                            format->streaming_interface = streaming;
                            uvc_add_vs_format(streaming, format);
                        }
                    }   break;

                    case VS_FRAME_UNCOMPRESSED:
                        uvc_add_vs_frame(uvc_get_last_format(streaming), uvc_vs_frame_uncompressed(buffer));
                        break;

                    case VS_FORMAT_H264: {
                        struct uvc_vs_format_descriptor *format = uvc_parse_vs_format_h264(buffer);
                        if (format) {
                            format->streaming_interface = streaming;
                            uvc_add_vs_format(streaming, format);
                        }
                    }   break;

                    case VS_FRAME_H264:
                        uvc_add_vs_frame(uvc_get_last_format(streaming), uvc_parse_vs_frame_h264(buffer));
                        break;

                    case VS_FORMAT_MPEG2TS:
                        LOGW("VS_FORMAT_MPEG2TS is not implements yet.");
                        break;

                    case VS_FORMAT_VP8:
                        LOGW("VS_FORMAT_VP8 is not implements yet.");
                        break;

                    case VS_FRAME_VP8:
                        LOGW("VS_FRAME_VP8 is not implements yet.");
                        break;

                    case VS_STILL_IMAGE_FRAME:
                        streaming->frame_still = uvc_parse_vs_still_image_frame(buffer);
                        break;

                    case VS_COLORFORMAT:
                        uvc_add_vs_color_matching(streaming, uvc_parse_vs_color_matching(buffer));
                        break;

                    default:
                        LOGW("UNKNOWN CS_INTERFACE (VS). %02X", bDescriptorSubtype);
                        break;
                }
            }   break;

            default:
                LOGW("UNKNOWN DESCRIPTOR (VS). %02X", bDescriptorType);
                break;
        }

        buffer += bLength;
        skipLength += bLength;
        length -= bLength;
    }
    return skipLength;
}


static void uvc_free_altsetting_descriptor(struct uvc_video_streaming_altsetting *altsetting) {
    if (altsetting) {
        uvc_free_altsetting_descriptor(altsetting->next);
        SAFE_FREE(altsetting->interface);
        SAFE_FREE(altsetting->video_endpoint);
        SAFE_FREE(altsetting->still_endpoint);
        SAFE_FREE(altsetting);
    }
}

static uint32_t uvc_parse_altsetting_descriptor(struct uvc_video_streaming_altsetting *altsetting, uint8_t *buffer, int32_t length) {
    uint32_t skipLength = 0;
    while (length > 0) {
        uint8_t bLength = *buffer;
        uint8_t bDescriptorType = *(buffer + 1);

        switch (bDescriptorType) {
            case INTERFACE:
                // 次の Interface がきたので終了
                return skipLength;

            case ENDPOINT: {
                struct uvc_endpoint_descriptor *endpoint = uvc_parse_endpoint(buffer);
                if (endpoint == NULL) {
                    return skipLength;
                } else if (altsetting->video_endpoint == NULL) {
                    altsetting->video_endpoint = endpoint;
                } else if (altsetting->still_endpoint == NULL) {
                    altsetting->still_endpoint = endpoint;
                } else {
                    SAFE_FREE(endpoint);
                }
            }   break;

            default:
                LOGW("UNKNOWN DESCRIPTOR (ALTSETTING). %02X", bDescriptorType);
                break;
        }

        buffer += bLength;
        skipLength += bLength;
        length -= bLength;
    }
    return skipLength;
}



static void uvc_free_video_streaming(struct uvc_video_streaming_interface *streaming_interface) {
    if (streaming_interface) {
        uvc_free_video_streaming(streaming_interface->next);

        SAFE_FREE(streaming_interface->interface);

        uvc_free_vs_header(streaming_interface->header);
        uvc_free_vs_format(streaming_interface->format);
        uvc_free_vs_still_image_frame(streaming_interface->frame_still);
        uvc_free_vs_color_matching(streaming_interface->color_matching);

        SAFE_FREE(streaming_interface->still_endpoint);
        SAFE_FREE(streaming_interface);
    }
}


static uint32_t uvc_parse_descriptor_configuration(struct uvc_configuration_descriptor *configuration, uint8_t *buffer, int32_t length) {
    uint32_t skipLength = 0;
    while (length > 0) {
        uint32_t bLength = *buffer;
        uint8_t bDescriptorType = *(buffer + 1);

        switch (bDescriptorType) {
            case CONFIGURATION:
                // 次の Configuration がきたので終了
                return skipLength;

            case INTERFACE: {
                struct uvc_interface_descriptor *interface = uvc_parse_interface_descriptor(buffer);
                if (interface == NULL) {
                    return UVC_OUT_OF_MEMORY;
                } else if (interface->bInterfaceClass == CC_VIDEO) {
                    switch (interface->bInterfaceSubClass) {
                        case SC_VIDEOCONTROL: {
                            // INTERFACE の分だけ移動しておく
                            buffer += bLength;
                            skipLength += bLength;
                            length -= bLength;

                            configuration->control_interface = CALLOC(struct uvc_video_control_interface);
                            if (configuration->control_interface == NULL) {
                                return UVC_OUT_OF_MEMORY;
                            }

                            configuration->control_interface->interface = interface;
                            bLength = uvc_parse_vc_descriptor(configuration->control_interface, buffer, length);
                        }   break;

                        case SC_VIDEOSTREAMING: {
                            // INTERFACE の分だけ移動しておく
                            buffer += bLength;
                            skipLength += bLength;
                            length -= bLength;

                            if (interface->bAlternateSetting == 0) {
                                struct uvc_video_streaming_interface *streaming_interface = CALLOC(struct uvc_video_streaming_interface);
                                if (streaming_interface == NULL) {
                                    return UVC_OUT_OF_MEMORY;
                                }

                                streaming_interface->interface = interface;
                                bLength = uvc_parse_vs_descriptor(streaming_interface, buffer, length);
                                uvc_add_video_streaming_interface(configuration, streaming_interface);
                            } else {
                                struct uvc_video_streaming_altsetting *altsetting = CALLOC(struct uvc_video_streaming_altsetting);
                                if (altsetting == NULL) {
                                    return UVC_OUT_OF_MEMORY;
                                }

                                altsetting->interface = interface;
                                bLength = uvc_parse_altsetting_descriptor(altsetting, buffer, length);
                                uvc_add_uvc_streaming_altsetting(configuration, altsetting);
                            }
                        }   break;

                        case SC_VIDEO_INTERFACE_COLLECTION:
                            LOGW("SC_VIDEO_INTERFACE_COLLECTION is not implements yet.");
                            SAFE_FREE(interface);
                            break;

                        default:
                            LOGW("UNKNOWN bInterfaceSubClass: %02X", interface->bInterfaceSubClass);
                            SAFE_FREE(interface);
                            break;
                    }
                } else {
                    LOGW("UNKNOWN bInterfaceClass. %02X", interface->bInterfaceClass);
                    SAFE_FREE(interface);
                }
            }   break;


            case STRING: {
                LOGW("STRING DESCRIPTOR.");
            }   break;

            case ENDPOINT:
                LOGW("ENDPOINT.");
                break;

            case CS_INTERFACE:
                LOGW("CS_INTERFACE. sub:%02X", *(buffer + 2));
                break;

            default:
                LOGW("UNKNOWN DESCRIPTOR (CONFIG). %02X", bDescriptorType);
                break;
        }

        buffer += bLength;
        skipLength += bLength;
        length -= bLength;
    }
    return skipLength;
}


static void uvc_free_configuration_descriptor(struct uvc_configuration_descriptor *configuration) {
    if (configuration) {
        uvc_free_configuration_descriptor(configuration->next);

        uvc_free_video_control(configuration->control_interface);
        uvc_free_video_streaming(configuration->streaming_interface);
        uvc_free_altsetting_descriptor(configuration->altsetting);

        SAFE_FREE(configuration);
    }
}


////////////////////// public //////////////////////////


struct uvc_configuration_descriptor *uvc_get_configuration_descriptor(struct uvc_descriptor *descriptor, uint8_t config_id) {
    if (descriptor == NULL || descriptor->configuration == NULL) {
        return NULL;
    }

    struct uvc_configuration_descriptor *config = descriptor->configuration;
    while (config) {
        if (config->bConfigurationValue == config_id) {
            return config;
        }
        config = config->next;
    }
    return NULL;
}


uvc_result uvc_parse_descriptor(struct uvc_descriptor *descriptor, uint8_t *buffer, int32_t length) {
    if (descriptor == NULL) {
        LOGE("descriptor is NULL.");
        return UVC_ERROR;
    }

    if (buffer == NULL) {
        LOGE("buffer is NULL.");
        return UVC_ERROR;
    }

    while (length > 0) {
        uint32_t bLength = *buffer;
        uint8_t bDescriptorType = *(buffer + 1);

        switch (bDescriptorType) {
            case DEVICE:
                descriptor->device = uvc_parse_device_descriptor(buffer);
                if (descriptor->device == NULL) {
                    return UVC_OUT_OF_MEMORY;
                }
                break;

            case CONFIGURATION: {
                struct uvc_configuration_descriptor *configuration = uvc_parse_configuration_descriptor(buffer);
                if (descriptor->device == NULL) {
                    return UVC_OUT_OF_MEMORY;
                }

                // CONFIGURATION の分だけ移動しておく
                buffer += bLength;
                length -= bLength;

                bLength = uvc_parse_descriptor_configuration(configuration, buffer, length);
                uvc_add_configuration(descriptor, configuration);
            }   break;

            case INTERFACE_ASSOCIATION:
                descriptor->interface_association = uvc_parse_iad(buffer);
                if (descriptor->interface_association == NULL) {
                    return UVC_OUT_OF_MEMORY;
                }
                break;

            case STRING: {
                LOGD("STRING DESCRIPTOR.");
            }   break;

            case ENDPOINT:
                LOGD("ENDPOINT.");
                break;

            case CS_INTERFACE:
                LOGD("CS_INTERFACE. sub:%02X", *(buffer + 2));
               break;

            default:
                LOGD("UNKNOWN DESCRIPTOR. %02X", bDescriptorType);
                break;
        }
        buffer += bLength;
        length -= bLength;
    }

    return (descriptor->configuration &&
            descriptor->configuration->control_interface &&
            descriptor->configuration->streaming_interface) ? UVC_SUCCESS : UVC_ERROR;
}


void uvc_dispose_descriptor(struct uvc_descriptor *descriptor) {
    if (descriptor == NULL) {
        return;
    }

    SAFE_FREE(descriptor->device);
    SAFE_FREE(descriptor->interface_association);

    uvc_free_configuration_descriptor(descriptor->configuration);
}


struct uvc_vs_format_descriptor *uvc_find_format_descriptor(struct uvc_descriptor *descriptor, uint8_t config_id, uint8_t format_index) {
    if (descriptor == NULL) {
        return NULL;
    }

    struct uvc_configuration_descriptor *config = uvc_get_configuration_descriptor(descriptor,
                                                                                   config_id);
    if (config) {
        struct uvc_video_streaming_interface *streaming_interface = config->streaming_interface;
        while (streaming_interface) {
            struct uvc_vs_format_descriptor *format = streaming_interface->format;
            while (format) {
                if (format->bFormatIndex == format_index) {
                    return format;
                }
                format = format->next;
            }
            streaming_interface = streaming_interface->next;
        }
    }
    return NULL;
}


struct uvc_vs_frame_descriptor *uvc_find_frame_descriptor(struct uvc_descriptor *descriptor, uint8_t config_id, uint8_t format_index, uint8_t frame_index) {
    if (descriptor == NULL) {
        return NULL;
    }

    struct uvc_vs_format_descriptor *format = uvc_find_format_descriptor(descriptor, config_id, format_index);
    if (format == NULL) {
        return NULL;
    }

    struct uvc_vs_frame_descriptor *frame = format->frame;
    while (frame) {
        if (frame->bFrameIndex == frame_index) {
            return frame;
        }
        frame = frame->next;
    }
    return NULL;
}


uint16_t uvc_get_uvc_version(struct uvc_descriptor *descriptor, uint8_t config_id) {
    if (descriptor == NULL) {
        return 0;
    }

    struct uvc_configuration_descriptor *config = uvc_get_configuration_descriptor(descriptor,
                                                                                   config_id);
    if (config && config->control_interface && config->control_interface->header) {
        return config->control_interface->header->bcdUVC;
    }
    return 0;
}


struct uvc_video_control_interface *uvc_get_video_control_interface(struct uvc_descriptor *descriptor, uint8_t config_id) {
    struct uvc_configuration_descriptor *config = uvc_get_configuration_descriptor(descriptor, config_id);
    return config ? config->control_interface : NULL;
}


struct uvc_video_streaming_interface *uvc_get_video_streaming_interface(struct uvc_descriptor *descriptor, uint8_t config_id) {
    struct uvc_configuration_descriptor *config = uvc_get_configuration_descriptor(descriptor, config_id);
    return config ? config->streaming_interface : NULL;
}


struct uvc_video_streaming_altsetting *uvc_get_video_streaming_altsetting(struct uvc_descriptor *descriptor, uint8_t config_id) {
    struct uvc_configuration_descriptor *config = uvc_get_configuration_descriptor(descriptor, config_id);
    return config ? config->altsetting : NULL;
}


enum uvc_uncompressed_format uvc_get_uncompressed_format(struct uvc_vs_format_descriptor *format) {
    if (format->bDescriptorSubType != VS_FORMAT_UNCOMPRESSED) {
        return UNKNOWN;
    }

    struct uvc_vs_format_uncompressed_descriptor *uncompressed = (struct uvc_vs_format_uncompressed_descriptor *) format;

    if (memcmp(uncompressed->guidFormat, GUID_YUY2_UNCOMPRESSED, 16) == 0) {
        return YUY2;
    } else if (memcmp(uncompressed->guidFormat, GUID_NV12_UNCOMPRESSED, 16) == 0) {
        return NV12;
    } else if (memcmp(uncompressed->guidFormat, GUID_M420_UNCOMPRESSED, 16) == 0) {
        return M420;
    } else if (memcmp(uncompressed->guidFormat, GUID_I420_UNCOMPRESSED, 16) == 0) {
        return I420;
    }
    return UNKNOWN;
}
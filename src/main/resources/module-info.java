module havis.opcua.message.common {
    requires havis.opcua.message;
    requires java.logging;

    exports havis.opcua.message.common;
    exports havis.opcua.message.common.model;
    exports havis.opcua.message.common.serialize;
}
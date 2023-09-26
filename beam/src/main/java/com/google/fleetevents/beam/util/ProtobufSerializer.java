package com.google.fleetevents.beam.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;

public class ProtobufSerializer extends JsonSerializer {
  private final JsonFormat.Printer protobufJsonPrinter = JsonFormat.printer();

  @Override
  public void serialize(
      Object proto, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    // use JsonFormat to convert protos
    jsonGenerator.writeRawValue(protobufJsonPrinter.print((Message) proto));
  }
}

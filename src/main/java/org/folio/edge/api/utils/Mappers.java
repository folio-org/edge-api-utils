package org.folio.edge.api.utils;

import java.text.SimpleDateFormat;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;

/**
 * Provide {@link #jsonMapper} and {@link #xmlMapper}.
 */
public final class Mappers {
  public static final String XML_PROLOG = "<?xml version='1.0' encoding='UTF-8'?>\n";

  public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  public static final JsonMapper jsonMapper = JsonMapper.builder()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .defaultDateFormat(new SimpleDateFormat(DATE_FORMAT))
    .build();

  public static final XmlMapper xmlMapper = XmlMapper.builder()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .defaultDateFormat(new SimpleDateFormat(DATE_FORMAT))
    .build();

  private Mappers() {

  }
}

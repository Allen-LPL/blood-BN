package cn.iocoder.yudao.framework.common.util.json.databind;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 基于时间戳的 LocalDateTime 反序列化器
 *
 * 支持两种输入格式：
 * 1. 数字（epoch 毫秒）→ 保持原有行为
 * 2. 字符串（"yyyy-MM-dd HH:mm:ss"）→ 按格式解析
 *
 * 通过实现 {@link ContextualDeserializer}，支持 {@link JsonFormat} 注解
 * 指定自定义格式，与 {@link TimestampLocalDateTimeSerializer} 行为对称。
 *
 * @author 老五
 */
@Slf4j
public class TimestampLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>
        implements ContextualDeserializer {

    public static final TimestampLocalDateTimeDeserializer INSTANCE = new TimestampLocalDateTimeDeserializer(null);

    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 由 @JsonFormat(pattern=...) 指定的格式；null 表示使用默认格式
     */
    private final DateTimeFormatter formatter;

    public TimestampLocalDateTimeDeserializer() {
        this(null);
    }

    public TimestampLocalDateTimeDeserializer(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
                                                 BeanProperty property) {
        if (property != null) {
            JsonFormat ann = property.getAnnotation(JsonFormat.class);
            if (ann == null) {
                ann = property.getContextAnnotation(JsonFormat.class);
            }
            if (ann != null && ann.pattern() != null && !ann.pattern().isEmpty()) {
                try {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern(ann.pattern());
                    return new TimestampLocalDateTimeDeserializer(fmt);
                } catch (IllegalArgumentException ex) {
                    log.warn("[createContextual][无效的 @JsonFormat pattern: {}]", ann.pattern(), ex);
                }
            }
        }
        return this;
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 情况一：字符串 → 按格式解析
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String text = p.getText();
            if (text != null && !text.trim().isEmpty()) {
                DateTimeFormatter fmt = (formatter != null) ? formatter : DEFAULT_FORMATTER;
                return LocalDateTime.parse(text.trim(), fmt);
            }
        }
        // 情况二：数字 → 按 epoch 毫秒解析（保持原有行为）
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(p.getValueAsLong()), ZoneId.systemDefault());
    }

}

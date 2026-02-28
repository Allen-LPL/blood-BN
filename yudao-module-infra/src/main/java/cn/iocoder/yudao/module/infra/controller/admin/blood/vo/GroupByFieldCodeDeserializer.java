package cn.iocoder.yudao.module.infra.controller.admin.blood.vo;

import cn.iocoder.yudao.module.infra.controller.admin.blood.enums.GroupByFieldEnum;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;

public class GroupByFieldCodeDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token != null && token.isNumeric()) {
            return parser.getIntValue();
        }

        String raw = parser.getValueAsString();
        if (raw == null) {
            return (Integer) context.handleUnexpectedToken(Integer.class, parser);
        }

        String value = raw.trim();
        if (value.isEmpty()) {
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ignored) {
        }

        for (GroupByFieldEnum groupByFieldEnum : GroupByFieldEnum.values()) {
            if (groupByFieldEnum.name().equalsIgnoreCase(value)
                    || groupByFieldEnum.getAlias().equalsIgnoreCase(value)) {
                return groupByFieldEnum.getCode();
            }
        }

        throw JsonMappingException.from(parser, "不支持的 groupBy: " + raw);
    }

}

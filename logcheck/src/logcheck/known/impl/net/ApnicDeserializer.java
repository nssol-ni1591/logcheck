package logcheck.known.impl.net;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

import logcheck.util.NetAddr;

@SuppressWarnings("rawtypes")
public class ApnicDeserializer implements JsonbDeserializer<Map> {

	private Logger log = Logger.getLogger(ApnicDeserializer.class.getName());
	private static final String DUP_MSG = "duplicate key={0}, exists={1}, new={2}";
	private static final String REP_MSG = "replace key={0}, exists={1}, new={2}";

	private void duplicate(HashMap<String, String> map, String name, String value) {

		// 特定のキーの場合のみ値の置換を行う
		if ("inetnum".equals(name)) {
			if (!map.get(name).equals(value)) {
				log.log(Level.FINE, DUP_MSG, new Object[] { name, map.get(name), value });
			}
			NetAddr cur = new NetAddr(map.get(name));
			NetAddr rep = new NetAddr(value);
			if (!cur.equals(rep) && cur.within(rep)) {
				map.put(name, value);
				map.remove("descr");
				map.remove("country");
				log.log(Level.INFO, REP_MSG, new Object[] { name, map.get(name), value });
			}
		}
		else if ("descr".equals(name)) {
			if (!map.get(name).equals(value)) {
				log.log(Level.FINE, DUP_MSG, new Object[] { name, map.get(name), value });
			}
			if (value.contains("Inc")
					|| value.contains("INC")
					|| value.contains("LTD")
					|| value.contains("Limited")
					|| value.contains("Corp")
					|| value.contains("Company")
					|| value.contains("Telecom")) {
				map.put(name, value);
				log.log(Level.INFO, REP_MSG, new Object[] { name, map.get(name), value });
			}
		}
		else {
			if (!map.get(name).equals(value)) {
				// 必要としない属性なので出力レベルを落とす
				log.log(Level.FINEST, DUP_MSG, new Object[] { name, map.get(name), value });
			}
		}
	}
	private void attributes(HashMap<String, String> map, String json) {
    	try (Jsonb jsonb = JsonbBuilder.create()) {
    		Type hashListType = ArrayList.class;
    		List<Map<String, Object>> attrs = jsonb.fromJson(json, hashListType);

    		attrs.stream()
    			.filter(attr -> attr.containsKey("name"))
    			.forEach(attr -> {
    				String name = attr.get("name").toString().toLowerCase();
    				Object values = attr.get("values");

    				if (map.containsKey(name)) {
    					String value;
    					if (values instanceof List) {
    						value = ((List<?>) values).get(0).toString();
    					}
    					else {
    						value = values.toString();
    					}
    					duplicate(map, name, value);
    				}
					else if (values == null) {
						// Do nothing
					}
					else if (values instanceof List) {
						// キーが存在しないので値の置換を行う(List)
						List<?> l = (List<?>) values;
						if (l.size() == 1) {
							map.put(name, l.get(0).toString());
						}
						else {
							map.put(name, l.toString());
						}
					}
					else {
						// キーが存在しないので値の置換を行う(Object)
						map.put(name, values.toString());
					}
    			});
    	}
    	catch (Exception e) {
    		log.log(Level.SEVERE, "catch Exception", e);
    	}
	}

	@Override
	public Map<String, String> deserialize(JsonParser jsonParser,
			DeserializationContext deserializationContext,
			Type type) {

		HashMap<String, String> map = new HashMap<>();

		while (jsonParser.hasNext()) {
            JsonParser.Event event = jsonParser.next();
            if (event == JsonParser.Event.KEY_NAME) {
                String className = jsonParser.getString();

                jsonParser.next();
                String value = jsonParser.getValue().toString();

                switch (className) {
                case "attributes":
                	attributes(map, value);
                	break;
                case "objectType":
				case "primaryKey":
		    		String name = className.toLowerCase();
	    			if (!map.containsKey(name)) {
	    				// キーが存在しない場合のみ値の置換を行う
	    				map.put(className, value);
	    			}
	    			break;
				case "type":
				case "comments":
				default:
					// Do nothing
				}
            }
            else {
            	// Do nothing
            }
        }
		log.log(Level.FINE, "map={0}", map);
        return map;
	}

}

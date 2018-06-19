package logcheck.known.net.apnic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

import logcheck.util.net.NetAddr;

public class SearchMapDeserializer implements JsonbDeserializer<SearchMap> {

	final Logger log = Logger.getLogger(SearchMapDeserializer.class.getName());

	@Override
	public SearchMap deserialize(JsonParser jsonParser, DeserializationContext deserializationContext, Type type) {

		SearchMap map = new SearchMap();

		while (jsonParser.hasNext()) {
            JsonParser.Event event = jsonParser.next();
            if (event == JsonParser.Event.KEY_NAME) {
                String className = jsonParser.getString();

                jsonParser.next();
                String val = jsonParser.getValue().toString();

				switch (className) {
				case "attributes":
				{
					Jsonb jsonb = JsonbBuilder.create();
					Type hashListType = new ArrayList<Map<String, Object>>() {
						private static final long serialVersionUID = 1L;
						}.getClass().getGenericSuperclass();
					try {
						List<Map<String, Object>> attrs = jsonb.fromJson(val, hashListType);

					    attrs.stream()
					    	.filter(attr -> attr.containsKey("name"))
					    	.forEach(attr -> {
					    		String name = attr.get("name").toString().toLowerCase();
					    		Object values = attr.get("values");
				    			if (map.containsKey(name)) {	
				    				if ("inetnum".equals(name)) {
				    					log.log(Level.FINE, "duplicate key={0}, exists={1}, new={2}", new Object[] { name, map.get(name), values });
				    					String v;
				    					if (values instanceof List) {
				    						v = ((List<?>) values).get(0).toString();
				    					}
				    					else {
				    						v = values.toString();
				    					}
				    					NetAddr cur = new NetAddr(map.get(name).toString());
				    					NetAddr rep = new NetAddr(v.toString());
				    					if (cur != null && !cur.equals(rep) && cur.within(rep)) {
						    				SearchTextString st = new SearchTextString(v);
						    				map.put(name, st);
						    				map.remove("descr");
						    				map.remove("country");
					    					log.log(Level.INFO, "replace key={0}, exists={1}, new={2}", new Object[] { name, map.get(name), values });
				    					}
				    				}
				    				else if ("descr".equals(name)) {
				    					log.log(Level.FINE, "duplicate key={0}, exists={1}, new={2}", new Object[] { name, map.get(name), values });
				    					String value;
				    					if (values instanceof List) {
				    						value = ((List<?>) values).get(0).toString();
				    					}
				    					else {
				    						value = values.toString();
				    					}

				    					if (value.contains("Inc") 
				    							|| value.contains("INC")
				    							|| value.contains("LTD") 
												|| value.contains("Limited") 
												|| value.contains("Corp")
												|| value.contains("Company")
												|| value.contains("Telecom")
												) {
				    						SearchTextString st = new SearchTextString(value);
				    						map.put(name, st);
					    					log.log(Level.INFO, "replace key={0}, exists={1}, new={2}", new Object[] { name, map.get(name), value });
										}

				    				}
				    				else {
				    					log.log(Level.FINE, "duplicate key={0}, exists={1}, new={2}", new Object[] { name, map.get(name), values });
				    				}
				    			}
				    			else if (values == null) {
					    		}
					    		else if (values instanceof List) {
									List<?> l = (List<?>)values;
					    			if (l.size() == 1) {
					    				SearchTextString st = new SearchTextString(l.get(0).toString());
					    				map.put(name, st);
					    			}
					    			else {
					    				SearchTextAttributes links = new SearchTextAttributes();
					    				l.forEach(v -> links.add(v));
					    				map.put(name, links);
					    			}
					    		}
					    		else {
					    			SearchTextString st = new SearchTextString(values.toString());
					    			map.put(name, st);
					    		}
					    });
					} catch (JsonbException e) {
					    e.printStackTrace();
					}
					break;
				}
				case "objectType":
				case "primaryKey":
		    		String name = className.toLowerCase();
	    			if (!map.containsKey(name)) {
	    				SearchTextString sts = new SearchTextString(val);
	    				map.put(className, sts);
	    				log.log(Level.FINE, "update key={0}, exists={1}, new={2}", new Object[] { name, map.get(name), val });
	    			}
	    			break;
				case "type":
				case "comments":
				default:
				}
            }
            else {
            }
        }
		log.log(Level.FINE, "map={0}" + map);
        return map;
	}

}

package logcheck.known.net.rest;

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

public class SearchMapDeserializer implements JsonbDeserializer<SearchMap> {

//	@Inject private Logger log;
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
					@SuppressWarnings("serial")
					Type hashListType = new ArrayList<Map<String, Object>>() {}.getClass().getGenericSuperclass();
					try {
						List<Map<String, Object>> attrs = jsonb.fromJson(val, hashListType);
						//System.out.println("List-->:" + attrs);

					    attrs.stream()
					    	.filter(attr -> attr.containsKey("name"))
					    	.forEach(attr -> {
					    		String name = attr.get("name").toString().toLowerCase();
					    		Object values = attr.get("values");
				    			if (values == null) {
//				    				log.log(Level.INFO, "name={0}, attr={1}", new Object[] { name, attr });
					    		}
					    		else if (values instanceof List) {
									@SuppressWarnings("unchecked")
									List<Object> l = (List<Object>)values;
					    			if (l.size() == 1) {
					    				SearchTextString st = new SearchTextString(l.get(0).toString());
						    			if (map.containsKey(name)) {
						    				if (name.equals("descr")) {
							    				log.log(Level.INFO, "duplicate key={0}, exists={1}, new={2}", new Object[] { name, map.get(name), values });
						    				}
						    				else {
							    				map.put(name, st);
						    					log.log(Level.INFO, "update key={0}, exists={1}, new={2}", new Object[] { name, map.get(name), values });
						    				}
						    			}
					    			}
					    			else {
						    			// Listの場合は置換する
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
	    			if (map.containsKey(name)) {
	    				SearchTextString sts = new SearchTextString(val);
	    				map.put(className, sts);
	    				log.log(Level.INFO, "update key={0}, exists={1}, new={2}", new Object[] { name, map.get(name), val });
	    			}
	    			break;
				case "type":
				case "comments":
				default:
//					log.log(Level.INFO, "className={0}, val={1}", new Object[] { className, val });
				}
				//                }
//                catch (ClassNotFoundException e) {
//                	e.printStackTrace();
//                }
            }
            else {
            	//System.out.println("event=" + event);
            }
        }
//		log.log(Level.INFO, "map={0}" + map);
        return map;
	}

}

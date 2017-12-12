package cn.org.hentai.server.util.db;

import cn.org.hentai.server.model.Page;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by matrixy on 2017-03-06.
 */
public final class DbUtil
{
    private DbUtil() { }

    // static OrderService orderService;

    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(
                    new TypeToken<TreeMap<String, Object>>(){}.getType(),
                    new JsonDeserializer<TreeMap<String, Object>>() {
                        @Override
                        public TreeMap<String, Object> deserialize(
                                JsonElement json, Type typeOfT,
                                JsonDeserializationContext context) throws JsonParseException {

                            TreeMap<String, Object> treeMap = new TreeMap<>();
                            JsonObject jsonObject = json.getAsJsonObject();
                            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                            for (Map.Entry<String, JsonElement> entry : entrySet) {
                                Object ot = entry.getValue();
                                if(ot instanceof JsonPrimitive){
                                    treeMap.put(entry.getKey(), ((JsonPrimitive) ot).getAsString());
                                }else{
                                    treeMap.put(entry.getKey(), ot);
                                }
                            }
                            return treeMap;
                        }
                    }).create();
    private static Type typeToken = new TypeToken<TreeMap<String, Object>>(){}.getType();

    public static Object format(Object model, Object...args)
    {
        Object dataModel = model;
        JsonArray list = null;
        JsonObject json = null;

        if (args.length % 2 != 0) throw new RuntimeException("请提供偶数个参数");
        Map<String, Formatter> formatters = new HashMap<String, Formatter>();
        try
        {
            if (Page.class.isAssignableFrom(model.getClass()))
            {
                json = new Gson().toJsonTree(dataModel).getAsJsonObject();
                list = json.get("result").getAsJsonArray();
            }
            else if (List.class.isAssignableFrom(model.getClass()))
            {
                list = new Gson().toJsonTree(dataModel).getAsJsonArray();
            }
            else
            {
                list = new JsonArray();
                list.add(new Gson().toJsonTree(dataModel).getAsJsonObject());
            }

            for (int i = 0; i < args.length; i += 2)
            {
                Class cls = (Class) args[i + 1];
                formatters.put(args[i].toString(), (Formatter)cls.newInstance());
            }
            for (int i = 0; i < list.size(); i++)
            {
                JsonObject item = list.get(i).getAsJsonObject();
                for (int s = 0; s < args.length; s += 2)
                {
                    String key = args[s].toString();
                    JsonElement value = item.get(key);
                    if (null == value) continue;
                    String val = formatters.get(key).format(value.isJsonNull() ? null : value.getAsString());
                    item.addProperty(formatFieldName("fmt_" + key), val);
                }
            }

            if (Page.class.isAssignableFrom(model.getClass()))
            {
                return gson.fromJson(json, typeToken);
            }
            else if (List.class.isAssignableFrom(model.getClass()))
            {
                List<TreeMap> rst = new ArrayList<TreeMap>();
                for (int i = 0; i < list.size(); i++)
                {
                    rst.add((TreeMap) gson.fromJson(list.get(i), typeToken));
                }
                return rst;
            }
            else
            {
                return gson.fromJson(list.get(0), typeToken);
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    protected static String charQuote(String val)
    {
        if (null == val) return val;
        return val.replaceAll("'", "\\\\'");
    }

    protected static String formatFieldName(String name)
    {
        String newName = "";
        boolean found = false;
        if (name.indexOf('.') > -1) name = name.replaceAll("`?\\w+`?\\.", "");
        for (int i = 0; i < name.length(); i++)
        {
            char chr = name.charAt(i);
            if (chr == '_')
            {
                found = true;
                continue;
            }
            newName += found ? Character.toUpperCase(chr) : chr;
            found = false;
        }
        return newName;
    }

    protected static String toDBName(String name)
    {
        StringBuffer newName = new StringBuffer(32);
        for (int i = 0; i < name.length(); i++)
        {
            char chr = name.charAt(i);
            if (Character.isUpperCase(chr)) newName.append('_');
            newName.append(Character.toLowerCase(chr));
        }
        return newName.toString();
    }

    protected static String formatDate(Date date)
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    protected static String valueLiteral(Object data)
    {
        String val = "null";
        if (data != null)
        {
            // number, bigdecimal, boolean, string, date, timestamp
            if (Number.class.isInstance(data)) val = String.valueOf(data);
            else if (Boolean.class.isInstance(data)) val = String.valueOf(((Boolean)data) ? 1 : 0);
            else if (String.class.isInstance(data)) val = "".equals(String.valueOf(val).trim()) ? "" : "'" + String.valueOf(data) + "'";
            else if (Date.class.isInstance(data)) val = "'" + formatDate((Date)data) + "'";
            else if (BigDecimal.class.isInstance(data)) val = String.valueOf(data);
        }
        return val;
    }
}

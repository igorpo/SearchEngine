package remote.frontierServer;

import databases.S3Wrapper;
import org.apache.commons.codec.binary.Base32;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Created by igorpogorelskiy on 12/7/16.
 */
public class SerializableArrayList implements Serializable {

    private List<String> list;

    public SerializableArrayList(List<String> arg0) {
        list = arg0;
    }

    private List<String> getList() {
        return list;
    }

    @Override
    public String toString() {
        StringBuffer listStr = new StringBuffer();
        String comma = "";
        for (String s : list) {
            listStr.append(comma).append(s);
            comma = ",";
        }
        return listStr.toString();
    }

    public String serialize() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(this);
        objectOutputStream.close();
//        return new String(Base64.getEncoder().encode(
//                this.toString().getBytes(StandardCharsets.UTF_8)));
        return new Base32().encodeAsString(outputStream.toByteArray());
//        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    public static List<String> deserialize(String stringList) throws IOException, ClassNotFoundException {
//        byte[] data = Base64.getDecoder().decode(stringList);
        byte[] data = new Base32().decode(stringList.getBytes());

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(data));
        SerializableArrayList serList = (SerializableArrayList) inputStream.readObject();
        inputStream.close();
        return serList.getList();
    }



}

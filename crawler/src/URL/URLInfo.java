package url;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class URLInfo {
    private static final Log log = LogFactory.getLog(URLInfo.class);
    private static final String IS_WWW_REQUIRED = "#www-required";
    private String hostName;
    private int portNo;
    private String filePath;
    private boolean wasRedirected = false;

    /**
     * Constructor called with raw URL as input - parses URL to obtain host name and file path
     */
    public URLInfo(String docURL){
        if(docURL == null || docURL.equals(""))
            return;
        docURL = docURL.trim();
        if((!docURL.startsWith("http://") || docURL.length() < 8) && !docURL.startsWith("https://"))
            return;
        boolean isSecure = docURL.startsWith("https://");
        // Stripping off 'http://'
        if (docURL.startsWith("http://"))
            docURL = docURL.substring(7);
        else if (isSecure)
            docURL = docURL.substring(8);
        // If starting with 'www.' , stripping that off too
//        if(docURL.startsWith("www.") && !docURL.endsWith(IS_WWW_REQUIRED))
//            docURL = docURL.substring(4);
//        else if (docURL.endsWith(IS_WWW_REQUIRED)) {
//            docURL = docURL.replace(IS_WWW_REQUIRED, "");
//            wasRedirected = true;
//        }
        int i = 0;
        while(i < docURL.length()){
            char c = docURL.charAt(i);
            if(c == '/')
                break;
            i++;
        }
        String address = docURL.substring(0,i);
        if(i == docURL.length())
            filePath = "/";
        else
            filePath = docURL.substring(i); //starts with '/'
        if(address.equals("/") || address.equals(""))
            return;
        if(address.indexOf(':') != -1){
            String[] comp = address.split(":",2);
//            if (wasRedirected) hostName = comp[0].trim();
//            else hostName = "www." +  comp[0].trim();
            hostName = comp[0].trim();
            try{
                portNo = Integer.parseInt(comp[1].trim());
            }catch(NumberFormatException nfe){
                if (isSecure) portNo = 443;
                else portNo = 80;
            }
        }else{
//            if (wasRedirected) hostName = address;
//            else hostName = "www." + address;
            hostName = address;
            if (isSecure) portNo = 443;
            else portNo = 80;
        }
    }

    public URLInfo(String hostName, String filePath){
        this.hostName = hostName;
        this.filePath = filePath;
        this.portNo = 80;
    }

    public URLInfo(String hostName,int portNo,String filePath){
        this.hostName = hostName;
        this.portNo = portNo;
        this.filePath = filePath;
    }

    public String getHostName(){
        return hostName;
    }

    public void setHostName(String s){
        hostName = s;
    }

    public int getPortNo(){
        return portNo;
    }

    public void setPortNo(int p){
        portNo = p;
    }

    public String getFilePath(){
        return filePath;
    }

    public void setFilePath(String fp){
        filePath = fp;
    }
}

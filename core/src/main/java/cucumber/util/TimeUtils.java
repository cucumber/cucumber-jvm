package cucumber.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils
{
    public String getDateTimeFromTimeStamp(long timeStampMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.XXX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        return sdf.format(new Date(timeStampMillis));
    }
}

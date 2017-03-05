package tool;

import java.net.CacheRequest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by renmingxu on 2017/3/5.
 */
public class DateFormatTool {
    public static String dateString(Date date){
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};
        Calendar c1 = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        c1.setTime(date);
        int fy = c1.get(Calendar.YEAR);
        int m = c1.get(Calendar.MONTH);
        c1.setTime(new Date());
        int y = c1.get(Calendar.YEAR);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdf3 = new SimpleDateFormat("dd");
        String year = sdf1.format(date);
        String time = sdf2.format(date);
        String day = sdf3.format(date);
        String month = months[m];
        if (fy < y) {
            return year + " " + month + " " + day;
        } else {
            return month + " " + day + " " + time;
        }
    }
}

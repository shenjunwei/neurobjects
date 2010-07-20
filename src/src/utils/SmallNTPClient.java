package utils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;

import org.apache.commons.net.ntp.NtpUtils;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;


public final class SmallNTPClient {

	    /**
	     * Process <code>TimeInfo</code> object and print its details.
	     * @param info <code>TimeInfo</code> object.
	     */
	    public static java.util.Date getTime (TimeInfo info)
	    {
	    	NtpV3Packet message = info.getMessage();
	    	TimeStamp rcvNtpTime = message.getReceiveTimeStamp();
	        
	        return (rcvNtpTime.getDate());

	       

	        
	    }

}

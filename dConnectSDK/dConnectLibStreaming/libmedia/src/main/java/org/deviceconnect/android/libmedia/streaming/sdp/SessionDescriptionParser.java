package org.deviceconnect.android.libmedia.streaming.sdp;

import org.deviceconnect.android.libmedia.streaming.sdp.attribute.ControlAttribute;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.FormatAttribute;
import org.deviceconnect.android.libmedia.streaming.sdp.attribute.RtpMapAttribute;

public class SessionDescriptionParser {

    private SessionDescriptionParser() {
    }

    public static SessionDescription parse(String text) {
        SessionDescription sdp = new SessionDescription();

        Scanner scanner = new Scanner(text);
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (line.startsWith("v=")) {
                sdp.setVersion(Integer.parseInt(line.substring(2)));
            } else if (line.startsWith("b=")) {
                sdp.addBandwidth(new Bandwidth(line.substring(2)));
            } else if (line.startsWith("u=")) {
                sdp.setUrl(new Url(line.substring(2)));
            } else if (line.startsWith("e=")) {
                sdp.setEMail(new EMail(line.substring(2)));
            } else if (line.startsWith("p=")) {
                sdp.setPhoneNumber(new PhoneNumber(line.substring(2)));
            } else if (line.startsWith("k=")) {
                sdp.setEncryptionKey(new EncryptionKey(line.substring(2)));
            } else if (line.startsWith("o=")) {
                sdp.setOrigin(new Origin(line.substring(2)));
            } else if (line.startsWith("s=")) {
                sdp.setSessionName(new SessionName(line.substring(2)));
            } else if (line.startsWith("i=")) {
                sdp.setInformation(new Information(line.substring(2)));
            } else if (line.startsWith("c=")) {
                sdp.setConnection(new Connection(line.substring(2)));
            } else if (line.startsWith("t=")) {
                sdp.addTime(new Time(line.substring(2)));
            } else if (line.startsWith("a=")) {
                sdp.addAttribute(parseAttribute(line.substring(2)));
            } else if (line.startsWith("m=")) {
                MediaDescription md = new MediaDescription(line.substring(2));
                parseMediaDescription(scanner, md);
                sdp.addMediaDescriptions(md);
            }
        }
        return sdp;
    }

    private static void parseMediaDescription(Scanner scanner, MediaDescription md) {
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (line.startsWith("m=")) {
                scanner.preLine();
                return;
            } else if (line.startsWith("i=")) {
                md.setInformation(new Information(line.substring(2)));
            } else if (line.startsWith("k=")) {
                md.setEncryptionKey(new EncryptionKey(line.substring(2)));
            } else if (line.startsWith("b=")) {
                md.addBandwidth(new Bandwidth(line.substring(2)));
            } else if (line.startsWith("a=")) {
                md.addAttribute(parseAttribute(line.substring(2)));
            } else if (line.startsWith("c=")) {
                md.addConnection(new Connection(line.substring(2)));
            }
        }
    }

    private static Attribute parseAttribute(String line) {
        if (line.startsWith("rtpmap:")) {
            return new RtpMapAttribute(line.substring("rtpmap:".length()));
        } else if (line.startsWith("fmtp:")) {
            return new FormatAttribute(line.substring("fmtp:".length()));
        } else if (line.startsWith("control:")) {
            return new ControlAttribute(line.substring("control:".length()));
        } else {
            return new Attribute(line, null);
        }
    }

    private static class Scanner {
        private String[] lines;
        private int count;

        Scanner(String text) {
            lines = text.split("\r\n");
            count = 0;
        }

        String nextLine() {
            return lines[count++];
        }

        boolean hasNext() {
            return count < lines.length;
        }

        void preLine() {
            count--;
        }
    }
}

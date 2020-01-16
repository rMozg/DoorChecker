package ru.mozgovoy.oleg.myhome;

public class JeromeDriver {
    public static String IP_ADDR = "192.168.1.61";
    public static String PASSWORD = "1q";
    public static int PORT = 2424;

    public static DigitalResult checkDigitalPin(int inputPinNumber) {
        return checkDigitalPin(0, 0, inputPinNumber);
    }

    public static DigitalResult checkDigitalPin(int highVoltagePinNumber, int pullDownPinNumber, int inputPinNumber) {
        String r = "";
        try {
            NetClient nc = new NetClient(IP_ADDR, PORT);
            nc.sendDataWithString("$KE,PSW,SET," + PASSWORD + "\r\n");
            r = nc.receiveDataFromServer();
            if (highVoltagePinNumber != 0) {
                nc.sendDataWithString("$KE,WR," + highVoltagePinNumber + ",1\r\n");
                r = nc.receiveDataFromServer();
            }
            if (pullDownPinNumber != 0) {
                nc.sendDataWithString("$KE,WR," + pullDownPinNumber + ",0\r\n");
                r = nc.receiveDataFromServer();
            }
            nc.sendDataWithString("$KE,RD," + inputPinNumber + "\r\n");
            r = nc.receiveDataFromServer();
            nc.disConnectWithServer();
            String resTrueString = "#RD,"+String.format("%02d",inputPinNumber)+",1";
            String resFalseString = "#RD,"+String.format("%02d",inputPinNumber)+",0";
            if(r.equals(resTrueString)){
                return new DigitalResult(false, true, null);
            }
            else if(r.equals(resFalseString)){
                return new DigitalResult(false, false, null);
            }
            else{
                return new DigitalResult(true, false, r);
            }
        } catch (Exception exc) {
            return new DigitalResult(true, false, MyTools.getExceptionTextForSms(exc));
        }
    }

    public static class DigitalResult {
        public boolean hasError;
        public String errorText;
        public boolean state;

        public DigitalResult(boolean hasError, boolean state, String errorText) {
            this.hasError = hasError;
            this.state = state;
            this.errorText = errorText;
        }
    }


    public static AnalogResult checkAnalogPin(int highVoltagePinNumber, int lowVoltagePinNumber, int inputPinNumber) {
        String r = "";
        try {
            NetClient nc = new NetClient(IP_ADDR, PORT);
            nc.sendDataWithString("$KE,PSW,SET," + PASSWORD + "\r\n");
            r = nc.receiveDataFromServer();
            if (highVoltagePinNumber != 0) {
                nc.sendDataWithString("$KE,WR," + highVoltagePinNumber + ",1\r\n");
                r = nc.receiveDataFromServer();
            }
            if (lowVoltagePinNumber != 0) {
                nc.sendDataWithString("$KE,WR," + lowVoltagePinNumber + ",0\r\n");
                r = nc.receiveDataFromServer();
            }
            nc.sendDataWithString("$KE,ADC," + inputPinNumber + "\r\n");
            r = nc.receiveDataFromServer();
            nc.disConnectWithServer();
            String startOfRes = "#ADC,"+String.format("%01d",inputPinNumber)+",";
            if(r.startsWith(startOfRes)){
                return new AnalogResult(false, Integer.parseInt(r.substring(startOfRes.length())), null);
            }
            else{
                return new AnalogResult(true, 0, "неожиданный ответ: " + r);
            }
        } catch (Exception exc) {
            return new AnalogResult(true, 0, MyTools.getExceptionTextForSms(exc));
        }
    }

    public static class AnalogResult {
        public boolean hasError;
        public String errorText;
        public int value;

        public AnalogResult(boolean hasError, int value, String errorText) {
            this.hasError = hasError;
            this.value = value;
            this.errorText = errorText;
        }
    }

}

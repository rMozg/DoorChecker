package ru.mozgovoy.oleg.myhome;

/**
 * Считается, что для контроля двери стоит платка Kernelchip Jerome, к ней подключены датчики,
 * контролирующие дверь и замок:
 * замок, кнопка: пин 2 - выход на кнопку, пин 1 - вход с кнопки,
 * закрытие двери, датчик Холла: пин 7 - земля, пин 8 - питание, ADC1 - выход датчика
 * У всех кнопок подтяжка к пину 5 (на него выводим землю).
 * Настроены CAT-команды:
 * переход 0->1 на ноге 3 даёт 1 на ноге 21.
 * Питание: пин 16 - земля, пин 18 - пиатние 5В
 */
public class DoorMethods {

    private static final int DOOR_CLOSED_ADC_MIN = 510;

    public static String pubCheckLock() {
        try {
            JeromeDriver.DigitalResult res = checkLock();
            if (res.hasError) {
                return res.errorText;
            } else if (res.state) {
                return "замок закрыт";
            } else {
                return "замок открыт!";
            }
        } catch (Exception exc) {
            return MyTools.getExceptionTextForSms(exc);
        }
    }

    public static JeromeDriver.DigitalResult checkLock() {
        return JeromeDriver.checkDigitalPin(2, 5, 1);
    }

    public static String pubCheckDoor() {
        try {
            JeromeDriver.DigitalResult res = checkDoor();
            if (res.hasError) {
                return res.errorText;
            } else if (res.state) {
                return "дверь закрыта";
            } else {
                return "дверь открыта!";
            }
        } catch (Exception exc) {
            return MyTools.getExceptionTextForSms(exc);
        }
    }

    public static JeromeDriver.DigitalResult checkDoor() {
        JeromeDriver.AnalogResult result = JeromeDriver.checkAnalogPin(8, 7, 1);
        if (result.hasError) {
            return new JeromeDriver.DigitalResult(true, false, result.errorText);
        } else {
            return new JeromeDriver.DigitalResult(false, result.value > DOOR_CLOSED_ADC_MIN, null);
        }
    }

    public static String pubCheckSecure() {
        try {
            JeromeDriver.DigitalResult resLock = checkLock();
            JeromeDriver.DigitalResult resDoor = checkDoor();
            if (resLock.hasError || resDoor.hasError) {
                return resLock.errorText + "\r\n" + resDoor.errorText;
            } else {
                if (resLock.state) {
                    if (resDoor.state) {
                        return "всё закрыто";
                    } else {
                        return "открытая дверь при закрытом замке!";
                    }
                } else {
                    if (resDoor.state) {
                        return "открытый замок при закртой двери!";
                    } else {
                        return "всё открыто!";
                    }
                }
            }
        } catch (Exception exc) {
            return MyTools.getExceptionTextForSms(exc);
        }
    }
}
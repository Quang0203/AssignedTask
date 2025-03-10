package cviettel.loginservice.enums;

import cviettel.loginservice.configuration.message.LabelKey;
import lombok.AllArgsConstructor;
import lombok.Getter;

//@Getter
//@AllArgsConstructor
public enum MessageCode {

    MSG1000(LabelKey.MSG_1000),

    MSG1001(LabelKey.MSG_1001),

    MSG1002(LabelKey.MSG_1002),

    MSG1003(LabelKey.MSG_1003),

    MSG1004(LabelKey.MSG_1004),

    MSG1005(LabelKey.MSG_1005),

    MSG1006(LabelKey.MSG_1006),

    MSG1007(LabelKey.MSG_1007),

    MSG1008(LabelKey.MSG_1008),

    MSG1009(LabelKey.MSG_1009),

    MSG1010(LabelKey.MSG_1010),

    MSG1011(LabelKey.MSG_1011),

    MSG1012(LabelKey.MSG_1012),

    MSG1013(LabelKey.MSG_1013),

    MSG1014(LabelKey.MSG_1014),

    MSG1015(LabelKey.MSG_1015),

    MSG1016(LabelKey.MSG_1016),

    MSG1017(LabelKey.MSG_1017),

    MSG1018(LabelKey.MSG_1018),

    MSG1019(LabelKey.MSG_1019),

    MSG1020(LabelKey.MSG_1020),

    MSG1021(LabelKey.MSG_1021),

    MSG1022(LabelKey.MSG_1022),

    MSG1023(LabelKey.MSG_1023),

    MSG1024(LabelKey.MSG_1024);

    private String key;

    MessageCode(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

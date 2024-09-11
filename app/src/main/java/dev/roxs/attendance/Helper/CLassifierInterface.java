package dev.roxs.attendance.Helper;

import java.util.ArrayList;

public interface CLassifierInterface {
    class Recognition {
        private final String name;
        private final String regNo;
        private final String pin;
        private final Float distance;
        private ArrayList<float[][]> extra;
        public Recognition(final String name, final String regNo, final  String pin, final Float distance) {
            this.name = name;
            this.regNo = regNo;
            this.pin = pin;
            this.distance = distance;
            this.extra = null;
        }
        public void setExtra(ArrayList<float[][]> extra) {
            this.extra = extra;
        }
        public Object getExtra() {
            return this.extra;
        }
        @Override
        public String toString() {
            String resultString = "";
            if (name != null) {
                resultString += "[" + name + "] ";
            }

            if (regNo != null) {
                resultString += regNo + " ";
            }
            if(pin != null){
                resultString += pin +" ";
            }
            if (distance != null) {
                resultString += String.format("(%.1f%%) ", distance * 100.0f);
            }

            return resultString.trim();
        }
    }
}

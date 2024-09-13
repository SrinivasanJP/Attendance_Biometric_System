package dev.roxs.attendance.Helper;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public interface ClassifierInterface {
    class Recognition {
        private  Float distance;
        private List<Float> extra;
        private int row=0,col=0;

        public Recognition() {
        }

        public Recognition(final Float distance) {
            this.distance = distance;
            this.extra = null;
        }
        public void setExtra(List<Float> extra) {
            this.extra = extra;
        }
        public List<Float> getExtra() {
            return this.extra;
        }
        @Override
        public String toString() {
            String resultString = "";
            if (distance != null) {
                resultString += String.format("(%.1f%%) ", distance * 100.0f);
            }

            return resultString.trim();
        }
        public List<Float> flatten2DArray(float[][] array) {
            List<Float> flatList = new ArrayList<>();
            for (float[] row : array) {
                for (float value : row) {
                    flatList.add(value);
                }
            }
            return flatList;
        }
        public float[][] unflattenList(List<Float> flatList) {
            float[][] array = new float[1][192];
            int index = 0;
            for (int i = 0; i < 1; i++) {
                for (int j = 0; j < 192; j++) {
                    array[i][j] = flatList.get(index++);
                }
            }
            return array;
        }
    }
}

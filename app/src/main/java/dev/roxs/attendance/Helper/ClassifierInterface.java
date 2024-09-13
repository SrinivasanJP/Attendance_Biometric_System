package dev.roxs.attendance.Helper;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public interface ClassifierInterface {
    class Recognition {
        private  Float distance;
        private List<Double> extra;
        private int row=0,col=0;

        public Recognition() {
        }

        public Recognition(final Float distance) {
            this.distance = distance;
            this.extra = null;
        }
        public void setExtra(List<Double> extra) {
            this.extra = extra;
        }
        public List<Double> getExtra() {
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
        public List<Double> flatten2DArray(float[][] array) {
            List<Double> flatList = new ArrayList<>();
            for (float[] row : array) {
                this.row++;
                for (double value : row) {
                    flatList.add(value);
                    this.col++;
                }
            }
            return flatList;
        }
        public double[][] unflattenList(List<Double> flatList) {
            double[][] array = new double[this.row][this.col];
            int index = 0;
            for (int i = 0; i < this.row; i++) {
                for (int j = 0; j < this.col; j++) {
                    array[i][j] = flatList.get(index++);
                }
            }
            return array;
        }
    }
}

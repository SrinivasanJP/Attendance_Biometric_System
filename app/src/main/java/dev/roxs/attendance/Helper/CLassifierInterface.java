package dev.roxs.attendance.Helper;

import java.util.ArrayList;
import java.util.List;

public interface CLassifierInterface {
    class Recognition {
        private final Float distance;
        private List<Float> extra;
        private int row=0,col=0;
        public Recognition(final Float distance) {
            this.distance = distance;
            this.extra = null;
        }
        public void setExtra(List<Float> extra) {
            this.extra = extra;
        }
        public Object getExtra() {
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
                this.row++;
                for (float value : row) {
                    flatList.add(value);
                    this.col++;
                }
            }
            return flatList;
        }
        public float[][] unflattenList(List<Float> flatList) {
            float[][] array = new float[this.row][this.col];
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

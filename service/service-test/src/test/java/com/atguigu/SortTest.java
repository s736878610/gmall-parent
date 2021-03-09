package com.atguigu;

public class SortTest {

    public static void main(String[] args) {
        int[] arrays = {4, 35, 14, 11, 55};
//        quickSort(arrays, 0, arrays.length - 1);
//        for (int arr : arrays) {
//            System.out.println(arr);
//        }
//        System.exit(0);

        mergeSort(arrays,0, arrays.length-1);
        for(int i : arrays) {
            System.out.println(i);
        }

//        System.out.println(power(5, 3));
    }

    /**
     * 快速排序
     *
     * @param arrays
     * @param start
     * @param end
     */
    public static void quickSort(int[] arrays, int start, int end) {
        if (end < start) return;
        int mid = handle(arrays, start, end);
        quickSort(arrays, start, mid - 1);
        quickSort(arrays, mid + 1, end);
    }

    private static int handle(int[] arrays, int start, int end) {
        int index = start;
        int temp = arrays[end];
        for (int i = start; i < end; i++) {
            if (arrays[i] < temp) {
                if (index != i) {
                    swap(arrays, i, index);
                }
                index++;
            }
        }
        if (index != end) {
            swap(arrays, index, end);
        }
        return index;
    }

    public static void swap(int[] arrays, int i, int j) {
        /**
         * 利用异或运算符交换a和b的值
         *      a= a^b;
         *      b= b^a;
         *      a= a^b;
         */
        arrays[i] = arrays[i] ^ arrays[j];
        arrays[j] = arrays[i] ^ arrays[j];
        arrays[i] = arrays[i] ^ arrays[j];
    }

    /**
     * 归并排序
     *
     * @param arrays
     * @param start
     * @param end
     */
    public static void mergeSort(int[] arrays, int start, int end) {
        if (end <= start) return;
        int mid = (start + end) >>> 1;// 无符号右移一位相当于除2，右移n位相当于除以2的n次方
        mergeSort(arrays, start, mid);
        mergeSort(arrays, mid + 1, end);
        merge(arrays, start, mid, end);
    }

    private static void merge(int[] arrays, int start, int mid, int end) {
        int[] temp = new int[end - start + 1];
        int index = 0;
        int i = start;
        int j = mid + 1;
        // 维护i和j两个指针，遍历两个数组，哪个指针上的数值小就放到临时数组里，指针继续后移
        while (i <= mid && j <= end) {
            temp[index++] = arrays[i] < arrays[j] ? arrays[i++] : arrays[j++];
        }
        // 如果指针i所在的数组遍历到了尽头，就不再放入临时数组
        while (i <= mid) {
            temp[index++] = arrays[i++];
        }
        // 如果指针j所在的数组遍历到了尽头，就不再放入临时数组
        while (j <= end) {
            temp[index++] = arrays[j++];
        }
        // 临时数组满了之后，将元素放回原数组
        for (int k = start; k <= end; k++) {
            arrays[k] = temp[k - start];
        }

    }

    /**
     * 求x的y次方
     * @param x
     * @param y
     * @return
     */
    public static int power(int x, int y) {
        if (y == 0) return 1;
        if (y == 1) return x;
        int result = power(x, y >> 1);
        if ((y & 1) == 0) {
            return result * result;
        } else {
            return result * result * x;
        }

    }


}

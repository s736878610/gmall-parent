package com.atguigu;

public class SortTest2 {

    public static void main(String[] args) {
        int[] array = {25, 12, 5, 55, 11};
//        quickSort(array, 0, array.length - 1);
//        for (int i : array) {
//            System.out.println(i);
//        }

        mergeSort(array, 0, array.length - 1);
        for (int i : array) {
            System.out.println(i);
        }
    }

    /**
     * 快速排序
     * 思想：任意找一个元素当"校准点"，比"校准点"小的元素往"校准点"左边放，比"校准点"大的元素往"校准点"右边放
     *
     * @param arrays
     * @param start  数组下标起点
     * @param end    数组下标终点
     */
    public static void quickSort(int[] arrays, int start, int end) {
        if (end < start) return;
        int mid = handler(arrays, start, end);
        quickSort(arrays, start, mid - 1);// "校准点"左边排序
        quickSort(arrays, mid + 1, end);// "校准点"右边排序
    }

    private static int handler(int[] arrays, int start, int end) {
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

    private static void swap(int[] arrays, int i, int j) {
        arrays[i] = arrays[i] ^ arrays[j];
        arrays[j] = arrays[i] ^ arrays[j];
        arrays[i] = arrays[i] ^ arrays[j];
    }


    /**
     * 归并排序的思想：将数组分成两半，左右排好序，再合并
     *
     * @param arrays
     * @param start
     * @param end
     */
    public static void mergeSort(int[] arrays, int start, int end) {
        if (end <= start) return;
        int mid = (start + end) >>> 1;// 找数组的中间点
        mergeSort(arrays, start, mid);// 左边排序
        mergeSort(arrays, mid + 1, end);// 右边排序
        merge(arrays, start, mid, end);// 合并
    }


    private static void merge(int[] arrays, int start, int mid, int end) {
        int[] temp = new int[end - start + 1];
        int tempIndex = 0;
        int i = start;
        int j = mid + 1;
        // 维护i和j两个指针，遍历两个数组，哪个指针上的数值小就放到临时数组里，指针继续后移
        while (i <= mid && j <= end) {
            temp[tempIndex++] = arrays[i] < arrays[j] ? arrays[i++] : arrays[j++];
        }
        // 如果指针i所在的数组遍历到了尽头，就不再放入临时数组
        while (i <= mid) {
            temp[tempIndex++] = arrays[i++];
        }
        // 如果指针j所在的数组遍历到了尽头，就不再放入临时数组
        while (j <= end) {
            temp[tempIndex++] = arrays[j++];
        }
        // 临时数组满了之后，将元素放回原数组
        for (int k = start; k <= end; k++) {
            arrays[k] = temp[k - start];
        }
    }


}

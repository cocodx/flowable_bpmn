package io.github.cocodx.test1;

/**
 * @author amazfit
 * @date 2022-09-23 下午9:24
 **/
public class Alo01 {

    public static void main(String[] args) {
        int[] a = new int[]{1,5,9,3,4};
        getStr(a);

        int[] b = new int[]{1,3,8,9,20};
        getStr(b);

        int[] c = new int[]{1,3,5};
        getStr(c);
    }

    public static String getStr(int[] a){
        StringBuffer stringBuffer = new StringBuffer();
        int lastNode = 0;
        for (int i=0;i<a.length;i++){
            if (i==0){
                lastNode = a[i];
                stringBuffer.append(a[i]);
                continue;
            }
            if (a[i]-lastNode==1){
                stringBuffer.append("-");
            }else{
                stringBuffer.append(",");
            }
            stringBuffer.append(a[i]);
            lastNode = a[i];
        }
        System.out.println(stringBuffer);
        return stringBuffer.toString();
    }
}

## 虚函数



## 数组和指针

```c++
int GetSize(int[] data) {
    return sizeof(data);
}
int _tmain(int argc, _TCHAR* argv[]) {
    int data1[] = {1,2,3,4,5};
    int size1 = sizeof(data1);
    
    int* data2 = data1;
    int size2 = sizeof(data2);
    
    int size3 = GetSize(data1);
    printf("%d, %d, %d", size1, size2, size3);
}
```

答案是“20,4,4”

- data1是一个数组，sizeof(data1)是求数组的大小。这个数组包含5个整数，每个整数4个字节，因此共占用20字节
- data2声明为指针，尽管它指向了数组data1的第一个数字，但它的本质仍然是一个指针。在32位的系统上，对任意指针求sizeof，得到的结果都是4.
- 在C/C++中，当数组作为函数的参数进行传递时，数组就自动退化为同类型的指针。因此。尽管函数GetSize的参数被声明为数组，但它会退化成为指针，size3的结果仍然为4
/* Arreglo de arreglos */
var a = [[1, 2, 3],
         [4, 5, 6]];

print(a[0]);
print(a[1]);
print(a[0][0]);
print(a[0][1]);
a[0][2] = 10;
a[1][0] = 20;


/* Ambiente con arreglo */
var b = {p: 1, q: [10, 20, 30]};

print(b.p);
print(b.q);
print(b.q[0]);
b.q[1] = 100;


/* Mayor anidamiento */
var c = {a: [[100, 200, 300],
             [400, 500, 600],
             [700, 800, 900]]};

print(c.a);
print(c.a[0]);
print(c.a[1][2]);
c.a[2][0] = -1;
pause(1);

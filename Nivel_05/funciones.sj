doble(x) {
  return x * 2;
}

print(doble(21));
print(doble(13));

parONon(n) {
  if (n % 2 == 0)
    return "par";
  return "non";
}

print(parONon(1));
print(parONon(6));

multiploDeTres(n) {
  if (n % 3 != 0)
    return;
  print(n + " es multiplo de 3");
}

var i = 1;
while (i < 10) {
  multiploDeTres(i);
  i = i + 1;
}

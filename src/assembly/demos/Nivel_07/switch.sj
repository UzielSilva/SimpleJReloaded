digito(d) {
  var mensaje;
  switch (d) {
    case 0:
      mensaje = "cero";
      break;
    
    case 1:
      mensaje = "uno";
      break;
      
    case 2:
      mensaje = "dos";
      break;
    
    case 3:
      mensaje = "tres";
      break;
    
    case 4:
      mensaje = "cuatro";
      break;
    
    case 5:
      mensaje = "cinco";
      break;
    
    case 6:
      mensaje = "seis";
      break;
    
    case 7:
      mensaje = "siete";
      break;
    
    case 8:
      mensaje = "ocho";
      break;
    
    case 9:
      mensaje = "nueve";
      break;
    
    default:
      mensaje = "otro";
  }
  print(mensaje);
}

digito(3);
digito(8);
digito(27);

numeros(n) {
  switch (n) {
    case 9:
      print("nueve");
    case 8:
      print("ocho");
    case 7:
      print("siete");
    case 6:
      print("seis");
    case 5:
      print("cinco");
    case 4:
      print("cuatro");
    case 3:
    case 2:
    case 1:
      print("tres, dos y uno");
  }
}

numeros(6);
numeros(2);
numeros(11);

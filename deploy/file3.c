#include <stdio.h>
#include "header.h"

void GetInfo(bool show)
{
     FILE* VD;
  
     struct files tmpfile;
     
     VD = fopen("VirtualDisc", "rb");
     if(!VD) 
     {
             if(show) printf("Wirtualny dysk nie istnieje\n");
             return;
     }
     fread(&info, sizeof(info), 1, VD);
     if(show)
     {
             printf("Rozmiar dysku: %dKB\n", info.size);
             printf("Liczba plikow: %d\n", info.filecount);
             printf("Ilosc wolnego miejsca: %dKB\n", info.freespace);
     }
     fclose(VD);
}
 

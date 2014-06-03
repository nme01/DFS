#include <stdio.h>
#include <stdlib.h>
#include "header.h"

int VDCreate(unsigned int size)
{
    FILE* VD;
    char* buffer;
    buffer = (char*)malloc(1);
    buffer[0] = 'x';
    
    //Utworz dysk o zadanym rozmiarze
    VD = fopen("VirtualDisc", "wb+");
    if(!VD) return 0;
    size *= 1024;
    fseek(VD, size-1, SEEK_SET);
    fwrite(buffer, 1, 1, VD);
    fseek(VD, 0, SEEK_SET);

    //zapisz na dysk informacje o systemie plikow
    info.size = size;
    info.freespace = size - 2048;
    info.filecount = 0;
    info.firstfree = 2048;
    printf("%d\n", info.size);
    fwrite(&info, sizeof(info), 1, VD);
    
    free(buffer);
    fclose(VD);
    return 1;
}

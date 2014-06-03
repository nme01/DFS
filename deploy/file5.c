#include <stdio.h>
#include <string.h>
#include "header.h"

bool CheckFilename(char* filename)
{
     FILE* VD;
     int i;
     struct files tmpfile;
     VD = fopen("VirtualDisc", "r");
     GetInfo(false);
     if(info.filecount == 0)
     {
           printf("Na dysku nie ma zadnych plikow\n");
           fclose(VD);
           return false;
     }
     fseek(VD, sizeof(info), SEEK_SET);
     for(i = 0; i < info.filecount; i++)
     {
           fread(&tmpfile, sizeof(tmpfile), 1, VD);
           if(strcmp(tmpfile.filename, filename) == 0)
           {
                 fclose(VD);
                 return true;
           }
     }
     fclose(VD);
     return false;
}

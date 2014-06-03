#include <stdio.h>


int VDDelete()
{
    if(!remove("VirtualDisc")) return 0;
    else return 1;
}

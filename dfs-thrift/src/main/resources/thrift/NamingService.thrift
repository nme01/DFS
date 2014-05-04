namespace java NamingService

typedef i32 int

service NamingService
{
        int put(1:string fileName),
        int get(1:string fileName)
}
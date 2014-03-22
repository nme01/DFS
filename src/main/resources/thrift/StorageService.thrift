namespace java StorageSerice

typedef i32 int

service StorageService
{
        void putFile(1:int fileId, 2:list<byte> body),
        list<byte> getFile(1:int fileId)
}
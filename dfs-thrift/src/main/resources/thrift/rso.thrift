namespace java rso.dfs.generated

typedef i32 int
typedef i64 long
typedef string IPType

enum ServerType {
    Slave,
    Master,
    Shadow
}

struct ServerStatus
{
    1: required ServerType type;
    2: required i32 filesNumber;
    3: required i64 freeSpace;
    4: required i64 usedSpace;
    5: required IPType serverIP;
}

struct SystemStatus {
    1:required i32 filesNumber;
    2:required list<ServerStatus> serversStatuses;
}

struct CoreStatus {
    1:IPType masterAddress;
    2:list<IPType> shadowsAddresses;
}

// describes part of file
struct FilePartDescription {
    1:int fileId;
    2:long offset;
}

//represents part of a file
//afaik, there exists a type which allows sending binary of arbitrary size
//(by that we don't have to pad the data)
struct FilePart {
    1:int fileId;
    2:long offset;
    3:binary data;
}

// communication initiated by new node
// new node sends to master file list
struct NewSlaveRequest {
    1:required IPType slaveIP;
    2:list<int> fileIds;
    3:list<long> fileSizes;
}
struct GetFileParams
{
    1:required i32 fileId;
    2:required IPType slaveIp;
 	3:required i64 size;
}

struct PutFileParams
{
    1:required bool canPut;
    2:required i32 fileId;
    3:required i64 size;
    4:required IPType slaveIp;
}

service Service
{
//returns administrative system status
SystemStatus getStatus(),

// returns list of file names 
list<string> listFileNames(),

//infrastucure building
//slave sends request to master to register to serve
CoreStatus registerSlave(1:NewSlaveRequest req),

//with this request master makes slave register again.
void forceRegister(1:CoreStatus status),

//master updates slaves status
void updateCoreStatus(1:CoreStatus status),

//master sends request to slave
void becomeShadow(1: CoreStatus status),

//client - anyone
CoreStatus getCoreStatus(), // returns master/shadows list

//ping server for checking whether it's alive
void pingServer(),

// file id, file size; master – slave
// force slave to be ready for file (fileId) which will be sent from client
void prepareForReceiving(1: int fileID, 2:long size),
//file id, slave ip
void replicate(1:int fileID, 2:IPType slaveIP, 3:long size),
IPType replicationFailure(1:int fileID, 2:IPType slaveIP),
//master - slave
bool isFileUsed(1:int fileID),
void removeFileSlave(1:int fileID),


GetFileParams getFile(1:string filepath),
GetFileParams getFileFailure(1:GetFileParams gfp),
PutFileParams putFile(1:string filepath, 2:long size),
PutFileParams putFileFailure(1:PutFileParams pfp),
bool removeFile(1:string filepath),

// Input: file ID (should be a structure? I think it’s too simple)
// returns: which part of file should be sent next
FilePartDescription sendFileToSlaveRequest(1: int fileId),

// Input: part of file which has to be sent
// returns: which part of file should be sent next, special value in case of finish
FilePartDescription sendFilePartToSlave(1: FilePart filePart),
FilePart getFileFromSlave(1: FilePartDescription filePartDescription),

void fileUploadSuccess(1:int fileID, 2: IPType slaveIP) 
}




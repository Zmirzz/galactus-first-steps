import socket,struct,sys
def send(s,kind,body,rid=1):
    raw=struct.pack('<ii',rid,kind)+body.encode()+b'\0\0'
    s.sendall(struct.pack('<i',len(raw))+raw)
def recv(s):
    def read(n):
        b=b''
        while len(b)<n:
            d=s.recv(n-len(b))
            if not d: raise ConnectionError('RCON disconnected')
            b+=d
        return b
    n=struct.unpack('<i',read(4))[0]; raw=read(n)
    return struct.unpack('<ii',raw[:8]),raw[8:-2].decode()
def commands(*commands):
    out=[]
    with socket.create_connection(('127.0.0.1',25579),timeout=30) as s:
        send(s,3,'galactus-local-lab'); response=recv(s)
        if response[0][1]==0: response=recv(s)
        if response[0][0]<0: raise RuntimeError('RCON authentication failed')
        for cmd in commands:
            send(s,2,cmd); out.append(recv(s)[1])
    return out
if __name__=='__main__':
    for cmd,out in zip(sys.argv[1:],commands(*sys.argv[1:])): print(cmd,'=>',out)

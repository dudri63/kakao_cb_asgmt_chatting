import socket
import sys
import threading


def send(client_sock):
    while True:
        try:
            send_data = bytes(input().encode())
            client_sock.sendall(send_data)
        except KeyboardInterrupt:
            sys.exit()


def rec(client_sock):
    while True:
        rec_data = client_sock.recv(1024).decode()
        print(rec_data)


# TCP Client
if __name__ == '__main__':
    client_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    arr = sys.argv[1].split(':')
    host = arr[0]
    port = int(arr[1])
    # host = 'localhost'
    # port = 8080
    client_sock.connect((host, port))
    print('Connected to', host, port)

    thread1 = threading.Thread(target=send, args=(client_sock,))
    thread1.start()

    thread2 = threading.Thread(target=rec, args=(client_sock,))
    thread2.start()

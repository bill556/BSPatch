BZIP2PATH=../bzip2-1.0.6
CC=gcc

CFLAGS          +=      -O3 -lbz2 -L${BZIP2PATH} -I ${BZIP2PATH}
  
PREFIX          ?=      /usr/local
INSTALL_PROGRAM ?=      ${INSTALL} -c -s -m 555
INSTALL_MAN     ?=      ${INSTALL} -c -m 444

all:            bsdiff bspatch
bsdiff:         bsdiff.c
	$(CC) bsdiff.c $(CFLAGS) -o bsdiff
bspatch:        bspatch.c
	$(CC) bspatch.c $(CFLAGS) -o bspatch

install:
	${INSTALL_PROGRAM} bsdiff bspatch ${PREFIX}/bin
	.ifndef WITHOUT_MAN
	${INSTALL_MAN} bsdiff.1 bspatch.1 ${PREFIX}/man/man1
	.endif
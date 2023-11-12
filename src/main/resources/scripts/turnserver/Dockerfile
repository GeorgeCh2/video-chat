FROM alpine:3.14

RUN apk add --no-cache build-base git autoconf automake libtool openssl-dev libevent-dev linux-headers curl

RUN curl -L -O https://github.com/coturn/coturn/archive/refs/tags/4.5.2.tar.gz \
    && tar xzf 4.5.2.tar.gz \
    && cd coturn-4.5.2 \
    && ./configure \
    && make \
    && make install

COPY ./turnserver.sh /turnserver.sh

ENV TURN_USERNAME test
ENV TURN_PASSWORD test
ENV NAT true

EXPOSE 3478 3478/udp

ENTRYPOINT ["/turnserver.sh"]
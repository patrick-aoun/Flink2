import pika


SERVER_QUEUE = 'resWiki'


def main():
    """ Here, Client sends "Marco" to RPC Server, and RPC Server replies with
    "Polo".

    NOTE Normally, the server would be running separately from the client, but
    in this very simple example both are running in the same thread and sharing
    connection and channel.

    """
    with pika.BlockingConnection() as conn:
        channel = conn.channel()

        # Set up server


        channel.basic_consume(SERVER_QUEUE, on_server_rx_rpc_request)


        # Set up client

        # NOTE Client must create its consumer and publish RPC requests on the
        # same channel to enable the RabbitMQ broker to make the necessary
        # associations.
        #
        # Also, client must create the consumer *before* starting to publish the
        # RPC requests.
        #
        # Client must create its consumer with auto_ack=True, because the reply-to
        # queue isn't real.

        channel.basic_publish(
            exchange='',
            routing_key="searchWiki",
            body='Marco',
            properties=pika.BasicProperties(reply_to='searchWiki'))

        channel.start_consuming()


def on_server_rx_rpc_request(ch, method_frame, properties, body):
    print('RPC Server got request: %s' % body)


    ch.basic_ack(delivery_tag=method_frame.delivery_tag)



if __name__ == "__main__":
    main()
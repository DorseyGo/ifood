# 1. Designing intensive data application

## 1.1 Reliable

> Continuing to work correctly, even when things go wrong

The things can go wrong are called `faults`, and systems that participate faults and can cope with them are called <i>fault-tolerance</i> or <i>resilience</i>.

>It is usually best to design fault-tolerance machinsims that prevent faults from causing failures.

Hard disks are reported as having a mean time to failure (MTTF) of about 10 to 50 years [5, 6].

Multi-machine redundency was only required by a small number of applications for which high availability was absolutely essential.

<b>Scalability</b> is the term we use to describe a system's ability to cope with `increased load`. 

<b>Load</b> can be described with a few numbers which we call `load parameters`. The best choice of parameters depends on the architecture of your system: it may be requests per second to a web server, the ratio of reads or writes in a database, the number of simultaneously active users in a chat room, the hit rate on a cache, or something else. 

Once you have described the load on your system, you can investigate what happens when the load increases:

-  when you increase a load parameter and keep the system resource (cpu, memory, network bandwidth, etc.) unchanged, how is the performance of your system affected?
- when you increase a load parameter, how much do you need to increase the resources if you want to keep performance unchanged?

> <i>Latency</i> and <i>response time</i> are not same. 
>
> - The <i>response time</i> is what the client sees: besides the actual time to process the request (the service time), it includes network delays and queueing delays. 
> - <i>Latency</i> is the duration that a request is <u>waiting</u> to be handled - during which it is latent, awaiting service.
# Docker

## Appendix

### Problems & Solves

***Problem***: docker容器无法通过IP访问宿主机

***Description***:

> 在使用docker的过程中，当需要在docker容器中访问宿主机的80端口，而这个端口是另外一个容器8080端口映射出去的。当在容器里通过docker的网桥*172.17.0.1* 访问宿主机时，居然发现：
>
> > Failed to connect to 172.17.0.1 port 80: no route to host

***Solves***: 

> 在/etc/firewalld/zones/public.xml里配置：
>
> > ```xml
> > <rule family="ipv4"></rule>
> > ```
> >
> > 

***Problem***: Docker进入容器后终端无法输入中文问题解决 

***Solves***:

> 在进入container的命令里加入`env LANG=C.UTF-8`
>
> > docker exec -it mysql env LANG=C.UTF-8 /bin/bash
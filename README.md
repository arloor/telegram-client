## windows10 tdlib jni使用

用于开发java电报客户端的脚手架

电报讨论组 https://t.me/popstary

## 目录说明

- docs/ 用于存放javaDocs，方便查阅TdApi
- lib/ 存放dll文件。运行前需要将`lib/dll/`下的三个dll文件复制到PATH路径下

## 运行说明(以Idea为例)

**1**. 克隆项目、切换到raw分支

```
git clone https://github.com/arloor/tdlib-use
```

**2**. 将`lib/dll/`下的三个dll文件放到环境变量PATH指定的文件夹中

**3**. 编辑run configuration

在VM options中增加：`-Djava.library.path=lib`，以指定tgjni.dll的查询路径为lib/

**4**. 运行Main

之后会看到如下：

```
Please enter phone number: 
```

此时就进入了telegram登录流程。

PS：为了China民众能够使用该应用，我添加了如下代碼：

```shell
client.send(new TdApi.AddProxy("127.0.0.1",1080,true,new TdApi.ProxyTypeSocks5()),null);
```

不是使用如上配置訪問國際互聯網的同学们，请修改成自己的配置。相信，想了解电报tdlib的同学肯定懂我在说什么（不要装了😂

登录成功后，会如下：

```shell script
Please enter phone number: +86139xxxxxxxx
Please enter authentication code: 12345
Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): sm 777000 电报牛逼
Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): 
Error {
  code = 5
  message = "Chat not found"
}

Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit):
```

该项目同时可以用于登录电报bot


```shell script
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
	"_doc": {
		"properties": {
			"senderId": {
				"type": "integer"
			},
			"chatId": {
				"type": "long"
			},
			"id": {
				"type": "long"
			},
			"time": {
				"type": "date"
			},
			"content": {
				"type": "text"
			}
		}
	}
}
}
```

```shell script
 curl localhost:9200/_search -d '
{
  "from": 0,
  "size": 100,
  "min_score": 4,
  "query": {
    "function_score": {
      "query": {
        "match_all": {}
      },
      "boost_mode": "replace",
      "functions": [
        {
          "script_score": {
            "script": {
              "source": "term_score",
              "lang": "expert_scripts",
              "params": {
                "field": [
                  "content^1"
                ],
                "query": "搜索"
              }
            }
          }
        }
      ]
    }
  }
}' -XPOST -H "content-type:application/json"
```

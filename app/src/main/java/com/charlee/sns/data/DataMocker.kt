package com.charlee.sns.data

import android.util.Log
import okhttp3.*

object DataMocker {
    private const val TAG = "DataMocker"

    fun dispatchMockResponse(request: Request): Response {
        val path = request.url().url().path
        Log.d(TAG, path)
        return when (path) {
            "/motusns/hot/messages" -> mockResponse(request, hotMessage())
            "/motusns/users/cards" -> mockResponse(request, userCards())
            "/motusns/now/campaigns" -> mockResponse(request, campaigns())
            else -> {
                mockResponse(request, hotMessage())
            }
        }
    }

    private fun mockResponse(request: Request, responseString: String): Response {
        val mediaType = MediaType.parse("application/json")
        return Response.Builder()
            .code(200)
            .protocol(Protocol.HTTP_1_1)
            .request(request)
            .message("")
            .addHeader("content-type", "application/json")
            .body(ResponseBody.create(mediaType, responseString.toByteArray())).build()
    }

    private fun campaigns(): String {
        return """
        {
  "errno": 0,
  "has_more": false,
  "campaigns": {
    "has_more": false,
    "last_id": "111",
    "data": [
      {
        "campaign_id": "2001",
        "campaign_type": 0,
        "start_time": 1638540705000,
        "end_time":   1638540705941,
        "version_min": "0",
        "version_max": "110",
        "to_type": 0,
        "viewer_num": 100,
        "participant_num": 10,
        "material_id": 0,
        "title": "活动标题",
        "img": "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201902%2F12%2F20190212114844_xlkyw.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1672663168&t=3fc21bfe00bd3c6daffb77984ca84e65",
        "url": "http://www.baidu.com",
        "gift": "iphone",
        "share": "share"
      }
    ]
  }
}
        """
    }

    private fun userCards(): String {
        return """
           {
  "errno": 0,
  "has_more": false,
  "cards": {
    "has_more": false,
    "last_id": "111",
    "data": [
      {
        "type": 0,
        "message": {
          "message_id": "101",
          "user": {
            "user_id":"001",
            "nick_name":"nick_name"
          },
          "content": {
            "image": {
              "url": "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fwww.xatao029.com%2Fzb_users%2Fupload%2F2022%2F03%2F202203211647867869812290.jpg&refer=http%3A%2F%2Fwww.xatao029.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1672662956&t=9877040ad16deada3075cb117b4b1a67",
              "width": 670,
              "height": 414
            },
            "video": {
              "cover": "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fwww.xatao029.com%2Fzb_users%2Fupload%2F2022%2F03%2F202203211647867869812290.jpg&refer=http%3A%2F%2Fwww.xatao029.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1672662956&t=9877040ad16deada3075cb117b4b1a67",
              "url_ori": "http://vjs.zencdn.net/v/oceans.mp4",
              "url_trans": "http://vjs.zencdn.net/v/oceans.mp4",
              "width": 720,
              "height": 300
            },
            "description": "demo message"
          }
        }
      },
      {
        "type": 0,
        "message": {
          "message_id": "102",
          "user": {
            "user_id":"001",
            "nick_name":"nick_name"
          },
          "content": {
            "image": {
              "url": "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201902%2F12%2F20190212114844_xlkyw.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1672663168&t=3fc21bfe00bd3c6daffb77984ca84e65",
              "width": 1269,
              "height": 1680
            },
            "video": {
              "cover": "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201902%2F12%2F20190212114844_xlkyw.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1672663168&t=3fc21bfe00bd3c6daffb77984ca84e65",
              "url_ori": "http://vjs.zencdn.net/v/oceans.mp4",
              "url_trans": "http://vjs.zencdn.net/v/oceans.mp4",
              "width": 720,
              "height": 300
            },
            "description": "demo message"
          }
        }
      }
    ]
  }
}
        """
    }
    private fun hotMessage(): String {
        return """{
  "errno": 0,
  "has_more": false,
  "messages": {
    "has_more": false,
    "last_id":"111",
    "data": [
      {
         "message_id": "001",
         "user": {
          "user_id":"001",
          "nick_name":"nick_name"
        },
          "content": {
             "image": {
                "url":"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fwww.xatao029.com%2Fzb_users%2Fupload%2F2022%2F03%2F202203211647867869812290.jpg&refer=http%3A%2F%2Fwww.xatao029.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1672662956&t=9877040ad16deada3075cb117b4b1a67",
                "width":670,
                "height":414
             },
             "video": {
              "cover":"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fwww.xatao029.com%2Fzb_users%2Fupload%2F2022%2F03%2F202203211647867869812290.jpg&refer=http%3A%2F%2Fwww.xatao029.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1672662956&t=9877040ad16deada3075cb117b4b1a67",
              "url_ori":"http://vjs.zencdn.net/v/oceans.mp4",
              "url_trans":"http://vjs.zencdn.net/v/oceans.mp4",
              "width":720,
              "height":300
             },
             "description":"demo message"
             } 
        },
        {
          "message_id": "002",
          "user": {
           "user_id":"001",
           "nick_name":"nick_name"
         },
           "content": {
              "image": {
                 "url":"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201902%2F12%2F20190212114844_xlkyw.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1672663168&t=3fc21bfe00bd3c6daffb77984ca84e65",
                 "width":1269,
                 "height":1680
              },
              "video": {
               "cover":"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201902%2F12%2F20190212114844_xlkyw.jpg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1672663168&t=3fc21bfe00bd3c6daffb77984ca84e65",
               "url_ori":"http://vjs.zencdn.net/v/oceans.mp4",
               "url_trans":"http://vjs.zencdn.net/v/oceans.mp4",
               "width":720,
               "height":300
              },
              "description":"demo message"
              } 
         },
         {
          "message_id": "003",
          "user": {
           "user_id":"001",
           "nick_name":"nick_name"
         },
           "content": {
              "image": {
                 "url":"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201604%2F20%2F20160420151835_a5cu3.jpeg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1672663169&t=1af5bc968535168a8fa3c93cc4ad3991",
                 "width":1200,
                 "height":2100
              },
              "video": {
               "cover":"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fc-ssl.duitang.com%2Fuploads%2Fitem%2F201604%2F20%2F20160420151835_a5cu3.jpeg&refer=http%3A%2F%2Fc-ssl.duitang.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1672663169&t=1af5bc968535168a8fa3c93cc4ad3991",
               "url_ori":"http://vjs.zencdn.net/v/oceans.mp4",
               "url_trans":"http://vjs.zencdn.net/v/oceans.mp4",
               "width":720,
               "height":300
              },
              "description":"demo message"
              } 
         }
    ]
  }
}
        """
    }
}
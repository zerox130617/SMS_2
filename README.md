# PCS期末報告
#### 組員
R08922062	林則瀚
R08944020	陳禹樵
## 1.發想
這次期末要求"以簡訊中帶有的特定字串來觸發APP的功能"，而我們討論之後覺得與常見的社交軟體(如LINE、Discord等)的機器人、Twitch的聊天室機器人很像。
這些機器人有個比較常見的應用就是撥放YouTube的影片或音樂，因此我們想做使用簡訊觸發的音樂播放機器人。
![](https://i.imgur.com/FzGBiDm.png)
*Discord的音樂機器人列表*
## 2.應用
### 應用場所
餐廳、酒吧、桌遊店等一般店家都能使用
### 顧客(Client端)使用說明：
- 向播放用的手機傳遞簡訊
- 傳遞$ytb [URL]，URL為youtube影片網址的v=之後的參數
![](https://i.imgur.com/TueSayd.png)
- Server端會回傳歌曲訊息
![](https://i.imgur.com/zAaMTmO.png)
- 傳遞$msg [string]，string為想讓機器人幫忙念的文字
![](https://i.imgur.com/ilRcXp9.png)
- 影片會加入Playlist中
![](https://i.imgur.com/g0KoaKg.png)
- 語音則會使當下正在撥放的音樂停下五秒，念出語音，之後繼續撥放音樂
### 店家(Server端)使用說明：
- Server端仍可以選擇音樂撥放
![](https://i.imgur.com/I8JdTqZ.png)
- 可以切歌以避免詭異的曲子(10hour、MEME曲等)
![](https://i.imgur.com/1hb1bj9.png)
### 特點：
- 顧客只需知道點歌用的手機號碼，不需要加入群組、特地下載新的應用程式、也不需要用網路
- 簡訊要花錢，理論上會點詭異歌曲的人會稍微變少，大量點歌的人可能也因此比較少
- 點一首歌或唸一段驚喜訊息只要一封簡訊的錢，不管要求婚、炒熱氣氛，怎麼想都非常划算
- 店家端也能進行一定程度的操作，甚至能有機會取得顧客之號碼電話，未來能進行一些商業上的操作(自動刪除簡訊的功能目前沒有做)
## 3.使用的Package、API
### 簡訊處理：
#### 自行implement簡訊處理
首先在manifest要權限
![](https://i.imgur.com/o4RDaE2.png)
並且設定broadcast receiver及intentfilter來取得intent
#### 繼承broadcast receiver的class
override原本的onReceive，對收到的bundle進行處理，從裡面的pdu(protocol description unit)取出SMS，並將簡訊內容、手機號碼透過main activity中的函數給main activity
![](https://imgur.com/Qy9ii2T.png)
*createFromPdu在後期的版本有修改*
這裡使用instance以達成activity、receiver class間的溝通
在main activity onCreate時將他的instance存成變數，這樣就能透過getInstance()使用main activity底下的函數
![](https://imgur.com/nRhi6ko.png)
*其實可以直接在activity下實做receiver的class，比較簡單，但是會太亂*

#### 權限要求：
![](https://imgur.com/a7RLqTj.png)
由於api level23以後視SMS服務為危險等級，要使用必須先檢查有沒有權限，沒有的話透過對話方塊問使用者
![](https://i.imgur.com/sRd1TcA.png)

override onRequestPermissionsResult，透過toast方塊顯示結果
#### 回傳簡訊：
![](https://i.imgur.com/JO6kUu4.png)

確認URL是正確的後回傳訊息
![](https://i.imgur.com/zAaMTmO.png)
另外由於簡訊有長度限制，故回傳兩封不同內容的簡訊
### URL相關：
簡訊傳來的URL透過Volley來向oEmbed丟https要求，注意的點是要用https，http會出錯
![](https://imgur.com/Crigomr.png)
他會回傳json檔如下
![](https://imgur.com/2tQ0zYT.png)
就能從中得到歌名，用來回傳簡訊及顯示在app上
### YouTube相關：
#### Library
![](https://i.imgur.com/Xi9uoZV.png)
Youtube有建了很不錯的reference網站，大部分的api使用只要看這邊就行了
#### 取得Google Api Key
![](https://i.imgur.com/5ChP4sj.png)
在初始化時需要填入這個
#### 設定manifest
網路連線及youtube api
![](https://imgur.com/EsOrpMK.png)
#### 初始化Youtube Player View
稍微有點複雜，需要處理
YoutubePlayerView：嵌到APP中的Youtube UI View，透過老朋友findViewById()來找
YoutubePlayer：操縱影片的各種method
![](https://imgur.com/dAsXQpm.png)
首先要先初始化YoutubePlayerView

![](https://imgur.com/PD4WRkI.png)
而要初始化需要API key以及一個OnInitializedListener()，
裡面需要重寫

public void onInitializationSuccess()
public void onInitializationFailure()
來分別處理初始化成功及失敗的case，在onInitializationSuccess()中
![](https://i.imgur.com/A8r2pa8.png)
我們創建新的youtube player的同時，還需要建立他的event listener來決定他在開始、暫停、結束時的行為。

#### Youtube Video List
*沒使用youtube的playlist api，自己用java做了*
#### Youtube Player Events
![](https://i.imgur.com/TVlEc5E.png)
有很多方便的函式可以override
### 文字轉語音(Text To Speech, TTS)相關：
#### 在manifest中宣告使用
![](https://i.imgur.com/LusFghe.png)
由於在android api level 18以後都有內建，不需要去google拿api key。
#### 語言設定
需要override TextToSpeech的onInit函式，在裡面設定語言。我們預設是中文(台灣)，所以其他語言的發音會有些困難。
![](https://i.imgur.com/19xWLDX.png)

## 4.其他(遇到的問題、困難等)
### Google Api Key：
常常一不小心傳到Github上，資安危機
#### 解決方法
1. 當你不小心將有api key的code commit上去時，可以馬上再commit一個沒key的，用
```
git amend
git rebase
squash
```
來將前幾個commit合併。
或者也可以直接刪掉該api key申請一個新的

2. 記得刪掉那部分的code再傳
### 簡訊長度：
嘗試傳超過長度的簡訊時，他會很乾脆地不理你
### JSON相關：
OEmbed回傳的是單一一個JSON檔，用JSON array接的話會出事
### Non-Blocking behavior:
大部分的api call都是non-blocking的，這點要特別小心。
建議所有的function call都要寫在invoke他的function裡面
### 向實驗室借的DEMO用電腦遭上鎖
說真的怎麼會有人把跟實驗室借的電腦上鎖，當自己的嗎
## 5.參與情況與貢獻度比例
參與情況：良好
林則瀚：起頭、簡訊處理、Youtube只做到初始化+純播放
陳禹樵：Youtube功能完善、文字轉語音、收尾
貢獻度比例：50 : 50

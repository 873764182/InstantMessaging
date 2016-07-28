package com.panxiong.instant.model;

import android.content.Context;
import android.database.Cursor;

import com.panxiong.instant.data.DataBaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MsgData {

    public Long _id;
    public Integer fromUserId;
    public Integer toUserId;
    public Long createTime;
    public Integer msgType;
    public String content;
    public String otherNote;

    @Override
    public String toString() {
        return "MsgData [_id=" + _id + ", fromUserId=" + fromUserId + ", toUserId=" + toUserId + ", createTime="
                + createTime + ", msgType=" + msgType + ", content=" + content + ", otherNote=" + otherNote + "]";
    }

    public Integer isRead = 0;  // 是否已读
    public boolean isSendOk = true;    // 是否发送成功

    /* 添加一条消息 */
    public static void saveMsgData(Context context, MsgData msgData) {
        if (getMsgData(context, msgData._id) != null) return;   // 数据库不做主键限制 避免多次保存
        String sql = "INSERT INTO "
                + "MsgData(_id, FromUserId, ToUserId, CreateTime, MsgType, Content, IsRead, OtherNote) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        DataBaseHelper.getDataBase(context).execSQL(sql, new Object[]{
                msgData._id.toString(), msgData.fromUserId.toString(), msgData.toUserId.toString(),
                msgData.createTime.toString(), msgData.msgType.toString(),
                msgData.content, msgData.isRead.toString(), msgData.otherNote}
        );
    }

    /* 删除一条消息 */
    public static void deleteMsgData(Context context, Long _id) {
        String sql = "DELETE FROM MsgData WHERE _id = ?";
        DataBaseHelper.getDataBase(context).execSQL(sql, new String[]{_id.toString()});
    }

    /* 删除自己消息 */
    public static void deleteUserMsgData(Context context, Integer userId) {
        String sql = "DELETE FROM MsgData WHERE (fromUserId = ? OR toUserId = ?)";
        DataBaseHelper.getDataBase(context).execSQL(sql, new String[]{userId.toString(),userId.toString()});
    }

    /* 删除所有消息 */
    public static void deleteAllMsgData(Context context) {
        String sql = "DELETE FROM MsgData";
        DataBaseHelper.getDataBase(context).execSQL(sql, null);
    }

    /* 获取一条消息 */
    public static MsgData getMsgData(Context context, Long _id) {
        String sql = "SELECT * FROM MsgData WHERE _id = ?";
        Cursor rs = DataBaseHelper.getDataBase(context).rawQuery(sql, new String[]{_id.toString()});
        List<MsgData> mds = cursorToList(rs);
        return (mds != null && mds.size() > 0) ? mds.get(0) : null;
    }

    /* 获取所有消息 */
    public static List<MsgData> getMsgDataList(Context context) {
        String sql = "SELECT * FROM MsgData ORDER BY CreateTime";
        Cursor rs = DataBaseHelper.getDataBase(context).rawQuery(sql, null);
        return cursorToList(rs);
    }

    /* 获取用户记录消息 */
    public static List<MsgData> getUserChatRecordMsg(Context context, Integer fromUserId, Integer toUserId) {
        String sql = "SELECT * FROM MsgData WHERE (FromUserId=? AND ToUserId=?) OR (FromUserId=? AND ToUserId=?) LIMIT 0,20 ";  // ORDER BY CreateTime
        Cursor rs = DataBaseHelper.getDataBase(context).rawQuery(sql, new String[]{fromUserId.toString(), toUserId.toString(),
                toUserId.toString(), fromUserId.toString()});
        return cursorToList(rs);
    }

    /*遍历结果集*/
    public static List<MsgData> cursorToList(Cursor rs) {
        if (rs == null) return null;
        List<MsgData> msgDatas = new ArrayList<>();
        while (rs.moveToNext()) {
            MsgData msgData = new MsgData();
            msgData._id = Long.parseLong(rs.getString(0).trim());
            msgData.fromUserId = rs.getInt(1);
            msgData.toUserId = rs.getInt(2);
            msgData.createTime = Long.parseLong(rs.getString(3).trim());
            msgData.msgType = rs.getInt(4);
            msgData.content = rs.getString(5);
            msgData.isRead = rs.getInt(6);
            msgData.otherNote = rs.getString(7);
            msgDatas.add(msgData);
        }
        return msgDatas;
    }
}

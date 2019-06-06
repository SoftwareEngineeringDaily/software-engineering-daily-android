package com.koalatea.sedaily.network.response

import com.koalatea.sedaily.database.table.Comment

data class CommentsResponse(val result: List<Comment>)
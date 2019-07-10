package com.koalatea.sedaily.network.response

import com.koalatea.sedaily.database.model.Comment

data class CommentsResponse(val result: List<Comment>)
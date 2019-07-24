package com.core.geology

class Run (file_name : String, layer : Int, type : String, type_num : String, thickness : String, bed_id : String, note : String, picture_name : String){
    var file_name : String = ""
    var layer : Int = 1
    var type : String = ""
    var type_num : String = ""
    var thickness : String = ""
    var bed_id : String = ""
    var note : String = ""
    var picture_name : String = ""

    init {
        this.file_name = file_name
        this.layer = layer
        this.type = type
        this.type_num = type_num
        this.thickness = thickness
        this.bed_id = bed_id
        this.note = note
        this.picture_name = picture_name
    }

    fun FiletoString() : String{
        var result = ""
        result = file_name+","+layer+","+type_num + ","+ type + "," + thickness+","+bed_id+","+note+","+picture_name

        return result
    }


}
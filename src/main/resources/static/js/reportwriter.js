function addTag() {
    const tagInput = document.getElementById("tag-input");
    const tagContainer = document.getElementById("tags-container");

    // 입력된 태그를 가져옵니다.
    const tag = tagInput.value.trim();

    // 태그가 비어있는 경우 추가하지 않습니다.
    if (!tag) {
        return;
    }

    // 최대 5개의 태그만 허용
    const existingTags = tagContainer.getElementsByClassName("tag");
    if (existingTags.length >= 5) {
        alert("최대 5개의 태그까지만 추가할 수 있습니다.");
        return;
    }

    // 새로운 태그를 만들고 추가합니다.
    const newTag = document.createElement("div");
    newTag.className = "tag";
    newTag.innerText = tag;
    tagContainer.appendChild(newTag);

    // 입력 필드를 비웁니다.
    tagInput.value = "";
}
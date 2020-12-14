import React, {useEffect} from 'react';

export default function Comments(props) {
  const rootElm = React.createRef();

  useEffect(() => {
    const utterances = document.createElement("script");

    utterances.src = "https://utteranc.es/client.js";
    utterances.setAttribute('repo', "alejandrohdezma/alejandrohdezma");
    utterances.setAttribute('issue-number', props.issue_number);
    utterances.setAttribute('label', "comment");
    utterances.setAttribute('theme', "preferred-color-scheme");
    utterances.crossOrigin = "anonymous";
    utterances.async = true;

    rootElm.current.appendChild(utterances);
  }, []);

  return (
    <div
      id="utterances_container"
      ref={rootElm}
    />
  );
};